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

import java.util.regex.Pattern;

/**
 * The purpose of this enum type is to declare Pattern integer constants in this num form.
 * <p> See Equivalent Embedded Flag Expression in {@link java.util.regex.Pattern}.
 * @author Tibor17
 * @version 0.1
 * @see Pattern
 * @since 0.1, Oct 2, 2011, 7:38:45 PM
 */
public enum MatchFlag {
    UNIX_LINES("(?d)", Pattern.UNIX_LINES),
    CASE_INSENSITIVE("(?i)", Pattern.CASE_INSENSITIVE),
    COMMENTS("(?x)", Pattern.COMMENTS),
    MULTILINE("(?m)", Pattern.MULTILINE),
    LITERAL(null, Pattern.LITERAL),
    DOTALL("(?s)", Pattern.DOTALL),
    UNICODE_CASE("(?u)", Pattern.UNICODE_CASE),
    CANON_EQ(null, Pattern.CANON_EQ);

    private final int flag;
    private final String expression;

    private MatchFlag(String expression, int flag) {
        this.flag = flag;
        this.expression = expression;
    }

    /**
     * Returns <tt>embedded flag expressions</tt> for this constant; or null if undefined.
     * The embedded flag expressions that correspond to these constants are the following:
     * <ul>
     *  <li>(?d) for UNIX_LINES
     *  <li>(?i) for CASE_INSENSITIVE
     *  <li>(?x) for COMMENTS
     *  <li>(?m) for MULTILINE
     *  <li>(?s) for DOTALL
     *  <li>(?u) for UNICODE_CASE
     *  <li>null for CANON_EQ and LITERAL
     * </ul>
     * <p>
     * @return <tt>embedded flag expressions</tt> for this constant; or null if undefined
     */
    public String getEmbeddedFlagExpression() {
        return expression;
    }

    int getJavaFlag() {
        return flag;
    }

    @Override
    public String toString() {
        return expression == null ? name() : expression;
    }
}