/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.scaling.core.api;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.JobPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;

import java.util.stream.Collectors;

/**
 * Registry repository API impl.
 */
@Slf4j
public final class RegistryRepositoryAPIImpl implements RegistryRepositoryAPI {
    
    private static final RegistryRepository REGISTRY_REPOSITORY = RegistryRepositoryHolder.getInstance();
    
    @Override
    public void persistJobPosition(final ScalingJob scalingJob) {
        JobPosition jobPosition = new JobPosition();
        jobPosition.setStatus(scalingJob.getStatus());
        jobPosition.setDatabaseType(scalingJob.getScalingConfig().getJobConfiguration().getDatabaseType());
        jobPosition.setIncrementalPositions(scalingJob.getIncrementalTasks().stream().collect(Collectors.toMap(ScalingTask::getTaskId, ScalingTask::getPosition)));
        jobPosition.setInventoryPositions(scalingJob.getInventoryTasks().stream().collect(Collectors.toMap(ScalingTask::getTaskId, ScalingTask::getPosition)));
        REGISTRY_REPOSITORY.persist(ScalingTaskUtil.getScalingListenerPath(scalingJob.getJobId(), scalingJob.getShardingItem()), jobPosition.toJson());
    }
    
    @Override
    public JobPosition getJobPosition(final long jobId, final int shardingItem) {
        String data = null;
        try {
            data = REGISTRY_REPOSITORY.get(ScalingTaskUtil.getScalingListenerPath(jobId, shardingItem));
        } catch (final NullPointerException ex) {
            log.info("job {}-{} without break point.", jobId, shardingItem);
        }
        return Strings.isNullOrEmpty(data) ? null : JobPosition.fromJson(data);
    }
}
