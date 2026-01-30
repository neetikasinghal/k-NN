/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.knn.index.mapper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.LeafReader;
import org.opensearch.index.mapper.FieldValueFetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opensearch.knn.index.vectorvalues.VectorValueExtractorStrategy.DISIVectorExtractor.extractFromBinaryDocValues;

/**
 * FieldValueFetcher for sorted numeric doc values, for a doc, values will be stored in
 * sorted order in lucene.
 *
 * @opensearch.internal
 */
public class BinaryDocValuesFetcher extends FieldValueFetcher {
    KNNVectorFieldType mappedFieldType;
    public BinaryDocValuesFetcher(KNNVectorFieldType mappedFieldType, String simpleName) {
        super(simpleName);
        this.mappedFieldType = mappedFieldType;
    }

    @Override
    public List<Object> fetch(LeafReader reader, int docId) throws IOException {
        List<Object> values = new ArrayList<>();
        try {
            final BinaryDocValues binaryDocValues = reader.getBinaryDocValues(mappedFieldType.name());
            if (binaryDocValues == null || !binaryDocValues.advanceExact(docId)) {
                return values;
            }
            Object extractedValue = extractFromBinaryDocValues(mappedFieldType.vectorDataType, binaryDocValues);
            if (extractedValue instanceof float[]) {
                values.addAll(Arrays.asList(ArrayUtils.toObject((float[]) extractedValue)));
            } else if (extractedValue instanceof byte[]) {
                values.addAll(Arrays.asList(ArrayUtils.toObject((byte[]) extractedValue)));
            } else {
                // should never land here
                values.add(extractedValue);
            }
        } catch (Exception e) {
            throw new IOException("Failed to read doc values for document " + docId + " in field " + mappedFieldType.name(), e);
        }
        return values;
    }
}
