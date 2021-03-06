/*
 * Copyright (C) 2017 University of South Florida.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usf.cutr.gtfsrtvalidator.test.rules;

import com.google.transit.realtime.GtfsRealtime;
import edu.usf.cutr.gtfsrtvalidator.api.model.ValidationRule;
import edu.usf.cutr.gtfsrtvalidator.test.FeedMessageTest;
import edu.usf.cutr.gtfsrtvalidator.test.util.TestUtils;
import edu.usf.cutr.gtfsrtvalidator.validation.rules.HeaderValidator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static edu.usf.cutr.gtfsrtvalidator.util.TimestampUtils.MIN_POSIX_TIME;
import static edu.usf.cutr.gtfsrtvalidator.validation.ValidationRules.E038;
import static edu.usf.cutr.gtfsrtvalidator.validation.ValidationRules.E039;

/**
 * Tests for rules implemented in HeaderValidator
 */
public class HeaderValidatorTest extends FeedMessageTest {

    public HeaderValidatorTest() throws Exception {
    }

    /**
     * E038 - Invalid header.gtfs_realtime_version
     */
    @Test
    public void testE038() {
        HeaderValidator headerValidator = new HeaderValidator();
        Map<ValidationRule, Integer> expected = new HashMap<>();

        GtfsRealtime.FeedHeader.Builder headerBuilder = GtfsRealtime.FeedHeader.newBuilder();

        // Valid version - no errors
        headerBuilder.setGtfsRealtimeVersion("1.0");
        feedMessageBuilder.setHeader(headerBuilder.build());

        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.clear();
        TestUtils.assertResults(expected, results);

        // Bad version - one error
        headerBuilder.setGtfsRealtimeVersion("2.0");
        feedMessageBuilder.setHeader(headerBuilder.build());

        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.put(E038, 1);
        TestUtils.assertResults(expected, results);

        // Bad version - one error
        headerBuilder.setGtfsRealtimeVersion("1");
        feedMessageBuilder.setHeader(headerBuilder.build());

        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.put(E038, 1);
        TestUtils.assertResults(expected, results);

        // Bad version - one error
        headerBuilder.setGtfsRealtimeVersion("abcd");
        feedMessageBuilder.setHeader(headerBuilder.build());

        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.put(E038, 1);
        TestUtils.assertResults(expected, results);

        clearAndInitRequiredFeedFields();
    }

    /**
     * E039 - FULL_DATASET feeds should not include entity.is_deleted
     */
    @Test
    public void testE039() {
        HeaderValidator headerValidator = new HeaderValidator();
        Map<ValidationRule, Integer> expected = new HashMap<>();

        GtfsRealtime.FeedHeader.Builder headerBuilder = GtfsRealtime.FeedHeader.newBuilder();
        headerBuilder.setGtfsRealtimeVersion("1.0");

        // FULL_DATASET feed without any entities - no errors
        headerBuilder.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        feedMessageBuilder.setHeader(headerBuilder.build());

        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.clear();
        TestUtils.assertResults(expected, results);

        // DIFFERENTIAL feed without any entities - no errors
        headerBuilder.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.DIFFERENTIAL);
        feedMessageBuilder.setHeader(headerBuilder.build());
        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.clear();
        TestUtils.assertResults(expected, results);

        // FULL_DATASET feed without is_deleted field in any entities - no errors
        headerBuilder.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        feedMessageBuilder.setHeader(headerBuilder.build());
        feedMessageBuilder.addEntity(feedEntityBuilder.build());
        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.clear();
        TestUtils.assertResults(expected, results);

        // DIFFERENTIAL feed without is_deleted field in any entities - no errors
        headerBuilder.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.DIFFERENTIAL);
        feedMessageBuilder.setHeader(headerBuilder.build());
        feedMessageBuilder.addEntity(feedEntityBuilder.build());
        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.clear();
        TestUtils.assertResults(expected, results);

        // FULL_DATASET feed with is_deleted field in an entity - 1 error
        headerBuilder.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        feedMessageBuilder.setHeader(headerBuilder.build());
        feedEntityBuilder.setIsDeleted(true);
        feedMessageBuilder.addEntity(feedEntityBuilder.build());
        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.put(E039, 1);
        TestUtils.assertResults(expected, results);

        // DIFFERENTIAL feed with is_deleted field in an entity - 0 errors
        feedMessageBuilder.clearEntity();
        headerBuilder.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.DIFFERENTIAL);
        feedMessageBuilder.setHeader(headerBuilder.build());
        feedEntityBuilder.setIsDeleted(true);
        feedMessageBuilder.addEntity(feedEntityBuilder.build());
        results = headerValidator.validate(MIN_POSIX_TIME, gtfsData, gtfsDataMetadata, feedMessageBuilder.build(), null);
        expected.clear();
        TestUtils.assertResults(expected, results);

        clearAndInitRequiredFeedFields();
    }
}
