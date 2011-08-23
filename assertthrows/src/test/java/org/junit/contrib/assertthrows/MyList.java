/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.junit.contrib.assertthrows;

import java.util.ArrayList;

/**
 * A helper class for the MyListTest.
 *
 * @author Thomas Mueller
 */
public class MyList {

    private ArrayList<String> list = new ArrayList<String>();
    private boolean closed;

    public void add(String item) {
        verifyItem(item);
        checkClosed();
        list.add(item);
    }

    public String get(int index) {
        checkClosed();
        return list.get(index);
    }

    public void set(int index, String item) {
        verifyItem(item);
        checkClosed();
        list.set(index, item);
    }

    public void remove(int index) {
        checkClosed();
        list.remove(index);
    }

    public int size() {
        checkClosed();
        return list.size();
    }

    public void close() {
        closed = true;
    }

    private void verifyItem(String item) {
        if (item == null) {
            throw new NullPointerException();
        }
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("closed");
        }
    }

}
