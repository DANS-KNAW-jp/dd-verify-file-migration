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
package nl.knaw.dans.filemigration.api;

import org.hsqldb.lib.StringUtil;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@IdClass(ActualFileKey.class)
@Table(name = "actual")
public class ActualFile {
  // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

  public ActualFile() {}

  public ActualFile(String doi, String actual_path, int major_version_nr, int minor_version_nr, String sha1_checksum, String storage_id) {
    this.doi = doi;
    this.actualPath = actual_path;
    this.majorVersionNr = major_version_nr;
    this.minorVersionNr = minor_version_nr;
    this.sha1Checksum = sha1_checksum;
    this.storageId = storage_id;
  }
  // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
  // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

  @Id
  @Column(length = 255)
  private String doi;

  @Id
  @Column(name="actual_path",length = 1024) // TODO basic_file_meta has only 1000
  private String actualPath;

  @Id
  @Column(name="major_version_nr")
  private int majorVersionNr;

  @Id
  @Column(name="minor_version_nr")
  private int minorVersionNr;

  @Column(name="sha1_checksum",length = 40)
  private String sha1Checksum = "";

  @Column(name="storage_id",length = 60)
  private String storageId = "";

  @Column(name="accessible_to")
  private String accessibleTo;

  @Column(name="publication_date")
  private String publicationDate;

  @Column()
  private String curator;

  @Nullable
  @Column(name="embargo_date")
  private String embargoDate;

  public int getMajorVersionNr() {
    return majorVersionNr;
  }

  public void setMajorVersionNr(int majorVersionNr) {
    this.majorVersionNr = majorVersionNr;
  }

  public void setMinorVersionNr(int minorVersionNr) {
    this.minorVersionNr = minorVersionNr;
  }

  public int getMinorVersionNr() {
    return minorVersionNr;
  }

  public String getActualPath() {
    return actualPath;
  }

  public void setActualPath(String actualPath) {
    this.actualPath = actualPath;
  }

  public String getStorageId() {
    return storageId;
  }

  public void setStorageId(String storageId) {
    this.storageId = storageId;
  }

  public String getSha1Checksum() {
    return sha1Checksum;
  }

  public void setSha1Checksum(String sha1Checksum) {
    this.sha1Checksum = sha1Checksum;
  }

  public void setAccessibleTo(String accessibleTo) {
    this.accessibleTo = accessibleTo;
  }

  public String getPublicationDate() {
    return publicationDate;
  }

  public void setPublicationDate(String publicationDate) {
    this.publicationDate = publicationDate;
  }

  public String getCurator() {
    return curator;
  }

  public void setCurator(String curator) {
    this.curator = curator;
  }

  public String getAccessibleTo() {
    return accessibleTo;
  }

  public void setAccessibleTo(boolean fileIsRestricted, boolean datasetHasAccessRequestEnabled) {
    if (!fileIsRestricted)
      this.accessibleTo = "ANONYMOUS";
    else if (datasetHasAccessRequestEnabled)
      this.accessibleTo = "RESTRICTED_REQUEST";
    else this.accessibleTo = "NONE";
  }

  @Nullable
  public String getEmbargoDate() {
    return embargoDate;
  }

  public void setEmbargoDate(@Nullable String dateAvailable) {
    if (!StringUtil.isEmpty(dateAvailable) && DateTime.now().compareTo(DateTime.parse(dateAvailable)) < 0)
      this.embargoDate = dateAvailable;
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  @Override
  public String toString() {
    return "ActualFile{" +
            "doi='" + doi + '\'' +
            ", actualPath='" + actualPath + '\'' +
            ", majorVersionNr=" + majorVersionNr +
            ", minorVersionNr=" + minorVersionNr +
            ", sha1Checksum='" + sha1Checksum + '\'' +
            ", storageId='" + storageId + '\'' +
            ", accessibleTo='" + accessibleTo + '\'' +
            ", publicationDate='" + publicationDate + '\'' +
            ", curator='" + curator + '\'' +
            ", embargoDate='" + embargoDate + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActualFile that = (ActualFile) o;
    return majorVersionNr == that.majorVersionNr && minorVersionNr == that.minorVersionNr && Objects.equals(doi, that.doi) && Objects.equals(actualPath, that.actualPath) && Objects.equals(sha1Checksum, that.sha1Checksum) && Objects.equals(storageId, that.storageId) && Objects.equals(accessibleTo, that.accessibleTo) && Objects.equals(publicationDate, that.publicationDate) && Objects.equals(curator, that.curator) && Objects.equals(embargoDate, that.embargoDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doi, actualPath, majorVersionNr, minorVersionNr, sha1Checksum, storageId, accessibleTo, publicationDate, curator, embargoDate);
  }
}
