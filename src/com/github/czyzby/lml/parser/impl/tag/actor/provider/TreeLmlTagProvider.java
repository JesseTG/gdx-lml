package com.github.czyzby.lml.parser.impl.tag.actor.provider;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.TreeLmlTag;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/** Provides Tree tags.
 *
 * @author MJ */
public class TreeLmlTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        return new TreeLmlTag(parser, parentTag, rawTagData);
    }
}
