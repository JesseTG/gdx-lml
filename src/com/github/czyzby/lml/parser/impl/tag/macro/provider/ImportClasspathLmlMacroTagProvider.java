package com.github.czyzby.lml.parser.impl.tag.macro.provider;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.macro.ImportClasspathLmlMacroTag;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/** Provides classpath import macro tags.
 *
 * @author MJ */
public class ImportClasspathLmlMacroTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        return new ImportClasspathLmlMacroTag(parser, parentTag, rawTagData);
    }
}
