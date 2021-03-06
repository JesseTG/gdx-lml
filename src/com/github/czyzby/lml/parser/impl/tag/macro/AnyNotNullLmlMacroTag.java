package com.github.czyzby.lml.parser.impl.tag.macro;

import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** Checks if any of the passed arguments is not null or boolean false. Evaluates to false if no non-null value is found
 * or receives 0 attributes.
 *
 * @author MJ */
public class AnyNotNullLmlMacroTag extends AbstractConditionalLmlMacroTag {
    public AnyNotNullLmlMacroTag(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected boolean checkCondition() {
        if (GdxArrays.isEmpty(getAttributes())) {
            // No arguments - this might happen even if the macro was properly invoked - with an argument that returns
            // empty string, for example. Assuming that no params = received null.
            return false;
        }
        for (final String attribute : getAttributes()) {
            if (isAction(attribute)) {
                final Object result = invokeAction(attribute);
                if (!isNullOrFalse(result)) {
                    // Method result not empty or "false".
                    return true;
                }
            }
            if (!isNullOrFalse(attribute)) {
                // Attribute is not blank, not equals "null" or "false" - assuming the attribute is valid.
                return true;
            }
        }
        return false;
    }
}
