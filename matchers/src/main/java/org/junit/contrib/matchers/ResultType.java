/*
 * The copyright holders of this work license this file to You under
 * the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.junit.contrib.matchers;

/**
 * The purpose of this private package enum is to specify
 * four post operations callable after successful regular
 * expression match in {@link IsRegex}.
 * <p/>
 * @author tibor17
 * @version 0.1
 * @see org.junit.contrib.matchers.IsRegex
 * @since 0.1, 26.12.2011, 11:22
 */
enum ResultType { GROUP, START, END, REGION }
