/*
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.filemigration.core;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import nl.knaw.dans.filemigration.api.ActualFile;
import nl.knaw.dans.filemigration.db.ActualFileDAO;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import nl.knaw.dans.lib.dataverse.model.dataset.SingleValueField;
import nl.knaw.dans.lib.dataverse.model.file.DataFile;
import nl.knaw.dans.lib.dataverse.model.file.Embargo;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import org.hsqldb.lib.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public class DataverseLoader {
    private static final Logger log = LoggerFactory.getLogger(DataverseLoader.class);

    private final ActualFileDAO actualFileDAO;
    private final DataverseClient client;

    public DataverseLoader(DataverseClient client, ActualFileDAO actualFileDAO) {
        this.actualFileDAO = actualFileDAO;
        this.client = client;
    }

    public void saveActual(ActualFile actual) {
        log.debug(actual.toString());
        actualFileDAO.create(actual);
    }

    public void loadFromDataset(String doi) {
        if (StringUtil.isEmpty(doi))
            return; // workaround
        log.info("Reading {} from dataverse", doi);
        List<DatasetVersion> versions;
        try {
            versions = client.dataset(doi).getAllVersions().getData();
        } catch (UnrecognizedPropertyException e) {
            log.error("Skipping {} {}", doi, e.getMessage());
            return;
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("not found"))
                log.error("{} {} {}", doi, e.getClass(), e.getMessage());
            else
                log.error("Could not retrieve file metas for DOI: {}", doi, e);
            return;
        }
        for (DatasetVersion dataset : versions) {
            int fileCount = 0;
            for (FileMeta fileMeta : dataset.getFiles()) {
                ActualFile actual = createActual(doi, dataset);
                actual.setAccessibleTo(fileMeta.getRestricted(), dataset.isFileAccessRequest());
                saveActual(fromFileMetadata(fileMeta, actual));
                ++fileCount;
            }
            log.info("Stored {} actual files for DOI {}, Version {}.{} State {}", fileCount, doi, dataset.getVersionNumber(), dataset.getVersionMinorNumber(), dataset.getVersionState());
        }
    }

    private ActualFile createActual(String doi, DatasetVersion dataset) {
        ActualFile actual = new ActualFile(doi);
        actual.setMajorVersionNr(dataset.getVersionNumber());
        actual.setMinorVersionNr(dataset.getVersionMinorNumber());
        actual.setCurator(getCitationDepositor(dataset, actual.getDoi()));
        return actual;
    }

    private ActualFile fromFileMetadata(FileMeta fileMeta, ActualFile actualFile) {
        DataFile dataFile = fileMeta.getDataFile();
        String dl = fileMeta.getDirectoryLabel();
        String actualPath = (dl == null ? "" : dl + "/") + fileMeta.getLabel();
        actualFile.setActualPath(actualPath);
        actualFile.setSha1Checksum(dataFile.getChecksum().getValue());
        actualFile.setStorageId(dataFile.getStorageIdentifier());
        Embargo embargo = dataFile.getEmbargo();
        if (embargo != null)
            actualFile.setEmbargoDate(embargo.getDateAvailable());
        return actualFile;
    }

    private String getCitationDepositor(DatasetVersion datasetVersion, String doi) {
        try {
            Stream<MetadataField> fieldStream = datasetVersion.getMetadataBlocks()
                    .get("citation").getFields().stream()
                    .filter(x -> "depositor".equals(x.getTypeName()));
            return ((SingleValueField) fieldStream.findFirst().get()).getValue();
        } catch (Exception e) {
            log.warn("no citation/depositor found for " + doi, e);
            return "";
        }
    }
}
