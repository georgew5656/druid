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

package org.apache.druid.indexing.common.actions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.druid.indexing.common.task.Task;
import org.apache.druid.timeline.DataSegment;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.annotation.Nullable;

import java.util.List;

public class RetrieveUnusedSegmentsAction implements TaskAction<List<DataSegment>>
{
  private final String dataSource;
  private final Interval interval;
  private final List<String> versions;
  private final Integer limit;
  private final DateTime maxUsedStatusLastUpdatedTime;

  @JsonCreator
  public RetrieveUnusedSegmentsAction(
      @JsonProperty("dataSource") String dataSource,
      @JsonProperty("interval") Interval interval,
      @JsonProperty("versions") @Nullable List<String> versions,
      @JsonProperty("limit") @Nullable Integer limit,
      @JsonProperty("maxUsedStatusLastUpdatedTime") @Nullable DateTime maxUsedStatusLastUpdatedTime
  )
  {
    this.dataSource = dataSource;
    this.interval = interval;
    this.versions = versions;
    this.limit = limit;
    this.maxUsedStatusLastUpdatedTime = maxUsedStatusLastUpdatedTime;
  }

  @JsonProperty
  public String getDataSource()
  {
    return dataSource;
  }

  @JsonProperty
  public Interval getInterval()
  {
    return interval;
  }

  @Nullable
  @JsonProperty
  public List<String> getVersions()
  {
    return versions;
  }

  @Nullable
  @JsonProperty
  public Integer getLimit()
  {
    return limit;
  }

  @Nullable
  @JsonProperty
  public DateTime getMaxUsedStatusLastUpdatedTime()
  {
    return maxUsedStatusLastUpdatedTime;
  }

  @Override
  public TypeReference<List<DataSegment>> getReturnTypeReference()
  {
    return new TypeReference<>() {};
  }

  @Override
  public List<DataSegment> perform(Task task, TaskActionToolbox toolbox)
  {
    return toolbox.getIndexerMetadataStorageCoordinator()
        .retrieveUnusedSegmentsForInterval(dataSource, interval, versions, limit, maxUsedStatusLastUpdatedTime);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" +
           "dataSource='" + dataSource + '\'' +
           ", interval=" + interval +
           ", versions=" + versions +
           ", limit=" + limit +
           ", maxUsedStatusLastUpdatedTime=" + maxUsedStatusLastUpdatedTime +
           '}';
  }
}
