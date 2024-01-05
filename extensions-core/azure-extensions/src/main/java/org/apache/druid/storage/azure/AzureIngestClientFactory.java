/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.storage.azure;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.apache.druid.data.input.azure.AzureInputSourceConfig;

import javax.annotation.Nullable;
import java.time.Duration;


public class AzureIngestClientFactory extends AzureClientFactory
{
  private final AzureInputSourceConfig azureInputSourceConfig;

  public AzureIngestClientFactory(AzureAccountConfig config, @Nullable AzureInputSourceConfig azureInputSourceConfig)
  {
    super(config);
    this.azureInputSourceConfig = azureInputSourceConfig;
  }

  @Override
  public BlobServiceClient buildNewClient(Integer retryCount, String storageAccount)
  {
    BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder()
        .endpoint("https://" + storageAccount + ".blob.core.windows.net");

    if (azureInputSourceConfig == null) {
      // If properties is not passed in inputSpec, use default azure credentials.
      return super.buildNewClient(retryCount, storageAccount);
    }

    if (azureInputSourceConfig.getKey() != null) {
      clientBuilder.credential(new StorageSharedKeyCredential(storageAccount, azureInputSourceConfig.getKey()));
    } else if (azureInputSourceConfig.getSharedAccessStorageToken() != null) {
      clientBuilder.sasToken(azureInputSourceConfig.getSharedAccessStorageToken());
    } else if (azureInputSourceConfig.shouldUseAzureCredentialsChain() != null) {
      DefaultAzureCredentialBuilder defaultAzureCredentialBuilder = new DefaultAzureCredentialBuilder()
          .managedIdentityClientId(config.getManagedIdentityClientId());
      clientBuilder.credential(defaultAzureCredentialBuilder.build());
    } else if (azureInputSourceConfig.getAppRegistrationClientId() != null && azureInputSourceConfig.getAppRegistrationClientSecret() != null) {
      clientBuilder.credential(new ClientSecretCredentialBuilder()
          .clientSecret(azureInputSourceConfig.getAppRegistrationClientSecret())
          .clientId(azureInputSourceConfig.getAppRegistrationClientId())
          .tenantId(azureInputSourceConfig.getTenantId())
          .build()
      );
    } else {
      // No credentials set in properties, use default azurecredentials.
      return super.buildNewClient(retryCount, storageAccount);
    }
    clientBuilder.retryOptions(new RetryOptions(
        new ExponentialBackoffOptions()
            .setMaxRetries(retryCount != null ? retryCount : config.getMaxTries())
            .setBaseDelay(Duration.ofMillis(1000))
            .setMaxDelay(Duration.ofMillis(60000))
    ));
    return clientBuilder.buildClient();
  }
}
