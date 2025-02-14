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

package nl.knaw.dans.filemigration;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DdVerifyFileMigrationConfiguration extends Configuration {

  @Valid
  @NotNull
  private DataSourceFactory easyDb = new DataSourceFactory();
  private DataSourceFactory fileMigrationDb = new DataSourceFactory();

  public DataSourceFactory getEasyDb() {
    return easyDb;
  }

  public DataSourceFactory getFileMigrationDb() {
    return fileMigrationDb;
  }

  public void setEasyDb(DataSourceFactory dataSourceFactory) {
    this.easyDb = dataSourceFactory;
  }

  public void setFileMigrationDb(DataSourceFactory dataSourceFactory) {
    this.fileMigrationDb = dataSourceFactory;
  }
}
