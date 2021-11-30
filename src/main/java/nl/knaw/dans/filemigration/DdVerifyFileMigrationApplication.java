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

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.dans.filemigration.api.EasyFile;
import nl.knaw.dans.filemigration.api.Expected;
import nl.knaw.dans.filemigration.cli.LoadFromFedoraCommand;

public class DdVerifyFileMigrationApplication extends Application<DdVerifyFileMigrationConfiguration> {

    private final HibernateBundle<DdVerifyFileMigrationConfiguration> easyBundle = new HibernateBundle<DdVerifyFileMigrationConfiguration>(EasyFile.class) {

      @Override
      public DataSourceFactory getDataSourceFactory(DdVerifyFileMigrationConfiguration configuration) {
        return configuration.getEasyDb();
      }
    };

    private final HibernateBundle<DdVerifyFileMigrationConfiguration> expectedBundle = new HibernateBundle<DdVerifyFileMigrationConfiguration>(Expected.class) {

      @Override
      public DataSourceFactory getDataSourceFactory(DdVerifyFileMigrationConfiguration configuration) {
        return configuration.getVerificationDatabase();
      }

      @Override
      public String name() {
        // the default "hibernate" is apparently required for at least one bundle: the easyBundle
        return "expectedBundle";
      }
    };

    public static void main(final String[] args) throws Exception {
        new DdVerifyFileMigrationApplication().run(args);
    }

    @Override
    public String getName() {
        return "DD Verify File Migration";
    }

    @Override
    public void initialize(final Bootstrap<DdVerifyFileMigrationConfiguration> bootstrap) {
        bootstrap.addBundle(easyBundle);
        bootstrap.addBundle(expectedBundle);
        bootstrap.addCommand(new LoadFromFedoraCommand(this, easyBundle, expectedBundle));
    }

    @Override
    public void run(final DdVerifyFileMigrationConfiguration configuration, final Environment environment) {
      environment.healthChecks().unregister("hibernate");
    }
}
