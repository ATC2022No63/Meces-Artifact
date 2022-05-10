/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.rest.handler.job.rescaling;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.runtime.rescale.RescaleSignal;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.rest.handler.async.AbstractAsynchronousOperationHandlers;
import org.apache.flink.runtime.rest.handler.async.AsynchronousOperationInfo;
import org.apache.flink.runtime.rest.handler.job.AsynchronousJobOperationKey;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.TriggerId;
import org.apache.flink.runtime.rest.messages.TriggerIdPathParameter;
import org.apache.flink.runtime.rest.messages.job.rescaling.RescaleTriggerRequestBody;
import org.apache.flink.runtime.rest.messages.job.rescaling.RescalingStatusHeaders;
import org.apache.flink.runtime.rest.messages.job.rescaling.RescalingStatusMessageParameters;
import org.apache.flink.runtime.rest.messages.job.rescaling.RescalingTriggerHeaders;
import org.apache.flink.runtime.rest.messages.job.rescaling.RescalingTriggerMessageParameters;
import org.apache.flink.runtime.rpc.RpcUtils;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;
import org.apache.flink.util.SerializedThrowable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Rest handler to trigger and poll the rescaling of a running job.
 */
public class RescalingHandlers extends AbstractAsynchronousOperationHandlers<AsynchronousJobOperationKey, Acknowledge> {

	/**
	 * Handler which triggers the rescaling of the specified job.
	 */
	public class RescalingTriggerHandler extends TriggerHandler<RestfulGateway, RescaleTriggerRequestBody, RescalingTriggerMessageParameters> {

		public RescalingTriggerHandler(
				GatewayRetriever<? extends RestfulGateway> leaderRetriever,
				Time timeout,
				Map<String, String> responseHeaders) {
			super(
				leaderRetriever,
				timeout,
				responseHeaders,
				RescalingTriggerHeaders.getInstance());
		}

		@Override
		protected CompletableFuture<Acknowledge> triggerOperation(HandlerRequest<RescaleTriggerRequestBody, RescalingTriggerMessageParameters> request, RestfulGateway gateway) throws RestHandlerException {
			final JobID jobId = request.getPathParameter(JobIDPathParameter.class);
			final RescaleSignal.RescaleSignalType rescaleSignalType = request.getRequestBody().getRescaleSignalType();
			final int globalParallelism = request.getRequestBody().getGlobalParallelism();
			final Map<String, Integer> parallelismList = request.getRequestBody().getParallelismList();
//			final List<Integer> queryParameter = request.getQueryParameter(
//				RescalingParallelismQueryParameter.class);

			log.info("Call dispatcher.");
			final CompletableFuture<Acknowledge> rescalingFuture = gateway.rescaleJob(
				jobId,
				rescaleSignalType,
				globalParallelism,
				parallelismList,
				RpcUtils.INF_TIMEOUT);

			log.info("Get future from dispatcher: " + rescalingFuture.hashCode());
			return rescalingFuture;
		}

		@Override
		protected AsynchronousJobOperationKey createOperationKey(HandlerRequest<RescaleTriggerRequestBody, RescalingTriggerMessageParameters> request) {
			final JobID jobId = request.getPathParameter(JobIDPathParameter.class);
			return AsynchronousJobOperationKey.of(new TriggerId(), jobId);
		}
	}

	/**
	 * Handler which reports the status of the rescaling operation.
	 */
	public class RescalingStatusHandler extends StatusHandler<RestfulGateway, AsynchronousOperationInfo, RescalingStatusMessageParameters> {

		public RescalingStatusHandler(
			GatewayRetriever<? extends RestfulGateway> leaderRetriever,
			Time timeout,
			Map<String, String> responseHeaders) {
			super(
				leaderRetriever,
				timeout,
				responseHeaders,
				RescalingStatusHeaders.getInstance());
		}

		@Override
		protected AsynchronousJobOperationKey getOperationKey(HandlerRequest<EmptyRequestBody, RescalingStatusMessageParameters> request) {
			final JobID jobId = request.getPathParameter(JobIDPathParameter.class);
			final TriggerId triggerId = request.getPathParameter(TriggerIdPathParameter.class);

			return AsynchronousJobOperationKey.of(triggerId, jobId);
		}

		@Override
		protected AsynchronousOperationInfo exceptionalOperationResultResponse(Throwable throwable) {
			return AsynchronousOperationInfo.completeExceptional(new SerializedThrowable(throwable));
		}

		@Override
		protected AsynchronousOperationInfo operationResultResponse(Acknowledge operationResult) {
			return AsynchronousOperationInfo.complete();
		}
	}
}