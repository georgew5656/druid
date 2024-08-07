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

package org.apache.druid.server.coordinator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.druid.java.util.common.ISE;
import org.apache.druid.server.coordinator.compact.CompactionStatistics;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class AutoCompactionSnapshot
{
  public enum AutoCompactionScheduleStatus
  {
    NOT_ENABLED,
    RUNNING
  }

  @JsonProperty
  private final String dataSource;
  @JsonProperty
  private final AutoCompactionScheduleStatus scheduleStatus;
  @JsonProperty
  private final long bytesAwaitingCompaction;
  @JsonProperty
  private final long bytesCompacted;
  @JsonProperty
  private final long bytesSkipped;
  @JsonProperty
  private final long segmentCountAwaitingCompaction;
  @JsonProperty
  private final long segmentCountCompacted;
  @JsonProperty
  private final long segmentCountSkipped;
  @JsonProperty
  private final long intervalCountAwaitingCompaction;
  @JsonProperty
  private final long intervalCountCompacted;
  @JsonProperty
  private final long intervalCountSkipped;

  public static Builder builder(String dataSource)
  {
    return new Builder(dataSource, AutoCompactionScheduleStatus.RUNNING);
  }

  @JsonCreator
  public AutoCompactionSnapshot(
      @JsonProperty @NotNull String dataSource,
      @JsonProperty @NotNull AutoCompactionScheduleStatus scheduleStatus,
      @JsonProperty long bytesAwaitingCompaction,
      @JsonProperty long bytesCompacted,
      @JsonProperty long bytesSkipped,
      @JsonProperty long segmentCountAwaitingCompaction,
      @JsonProperty long segmentCountCompacted,
      @JsonProperty long segmentCountSkipped,
      @JsonProperty long intervalCountAwaitingCompaction,
      @JsonProperty long intervalCountCompacted,
      @JsonProperty long intervalCountSkipped
  )
  {
    this.dataSource = dataSource;
    this.scheduleStatus = scheduleStatus;
    this.bytesAwaitingCompaction = bytesAwaitingCompaction;
    this.bytesCompacted = bytesCompacted;
    this.bytesSkipped = bytesSkipped;
    this.segmentCountAwaitingCompaction = segmentCountAwaitingCompaction;
    this.segmentCountCompacted = segmentCountCompacted;
    this.segmentCountSkipped = segmentCountSkipped;
    this.intervalCountAwaitingCompaction = intervalCountAwaitingCompaction;
    this.intervalCountCompacted = intervalCountCompacted;
    this.intervalCountSkipped = intervalCountSkipped;
  }

  @NotNull
  public String getDataSource()
  {
    return dataSource;
  }

  @NotNull
  public AutoCompactionScheduleStatus getScheduleStatus()
  {
    return scheduleStatus;
  }

  public long getBytesAwaitingCompaction()
  {
    return bytesAwaitingCompaction;
  }

  public long getBytesCompacted()
  {
    return bytesCompacted;
  }

  public long getBytesSkipped()
  {
    return bytesSkipped;
  }

  public long getSegmentCountAwaitingCompaction()
  {
    return segmentCountAwaitingCompaction;
  }

  public long getSegmentCountCompacted()
  {
    return segmentCountCompacted;
  }

  public long getSegmentCountSkipped()
  {
    return segmentCountSkipped;
  }

  public long getIntervalCountAwaitingCompaction()
  {
    return intervalCountAwaitingCompaction;
  }

  public long getIntervalCountCompacted()
  {
    return intervalCountCompacted;
  }

  public long getIntervalCountSkipped()
  {
    return intervalCountSkipped;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AutoCompactionSnapshot that = (AutoCompactionSnapshot) o;
    return bytesAwaitingCompaction == that.bytesAwaitingCompaction &&
           bytesCompacted == that.bytesCompacted &&
           bytesSkipped == that.bytesSkipped &&
           segmentCountAwaitingCompaction == that.segmentCountAwaitingCompaction &&
           segmentCountCompacted == that.segmentCountCompacted &&
           segmentCountSkipped == that.segmentCountSkipped &&
           intervalCountAwaitingCompaction == that.intervalCountAwaitingCompaction &&
           intervalCountCompacted == that.intervalCountCompacted &&
           intervalCountSkipped == that.intervalCountSkipped &&
           dataSource.equals(that.dataSource) &&
           scheduleStatus == that.scheduleStatus;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(
        dataSource,
        scheduleStatus,
        bytesAwaitingCompaction,
        bytesCompacted,
        bytesSkipped,
        segmentCountAwaitingCompaction,
        segmentCountCompacted,
        segmentCountSkipped,
        intervalCountAwaitingCompaction,
        intervalCountCompacted,
        intervalCountSkipped
    );
  }

  public static class Builder
  {
    private final String dataSource;
    private final AutoCompactionScheduleStatus scheduleStatus;

    private final CompactionStatistics compactedStats = new CompactionStatistics();
    private final CompactionStatistics skippedStats = new CompactionStatistics();
    private final CompactionStatistics waitingStats = new CompactionStatistics();

    private Builder(
        @NotNull String dataSource,
        @NotNull AutoCompactionScheduleStatus scheduleStatus
    )
    {
      if (dataSource == null || dataSource.isEmpty()) {
        throw new ISE("Invalid dataSource name");
      }
      if (scheduleStatus == null) {
        throw new ISE("scheduleStatus cannot be null");
      }

      this.dataSource = dataSource;
      this.scheduleStatus = scheduleStatus;
    }

    public void incrementWaitingStats(CompactionStatistics entry)
    {
      waitingStats.increment(entry);
    }

    public void incrementCompactedStats(CompactionStatistics entry)
    {
      compactedStats.increment(entry);
    }

    public void incrementSkippedStats(CompactionStatistics entry)
    {
      skippedStats.increment(entry);
    }

    public AutoCompactionSnapshot build()
    {
      return new AutoCompactionSnapshot(
          dataSource,
          scheduleStatus,
          waitingStats.getTotalBytes(),
          compactedStats.getTotalBytes(),
          skippedStats.getTotalBytes(),
          waitingStats.getNumSegments(),
          compactedStats.getNumSegments(),
          skippedStats.getNumSegments(),
          waitingStats.getNumIntervals(),
          compactedStats.getNumIntervals(),
          skippedStats.getNumIntervals()
      );
    }
  }
}
