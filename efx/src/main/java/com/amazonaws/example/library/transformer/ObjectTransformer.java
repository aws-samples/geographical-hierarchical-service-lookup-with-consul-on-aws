// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0


package com.amazonaws.example.library.transformer;

public interface ObjectTransformer<F, T> {
    T transform(F fObject);
}
