package com.github.czyzby.lml.parser.impl.tag.macro.provider;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.macro.ArgumentReplacementLmlMacroTag;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/** Provides argument replacement macro tags.
 *
 * @author MJ */
public class ArgumentReplacementLmlMacroTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        return new ArgumentReplacementLmlMacroTag(parser, parentTag, rawTagData);
    }
}
