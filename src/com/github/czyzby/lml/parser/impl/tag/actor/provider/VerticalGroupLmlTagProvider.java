package com.github.czyzby.lml.parser.impl.tag.actor.provider;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.VerticalGroupLmlTag;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/** Provides VerticalGroup tags.
 *
 * @author MJ */
public class VerticalGroupLmlTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        return new VerticalGroupLmlTag(parser, parentTag, rawTagData);
    }
}
