package com.github.czyzby.lml.parser.impl.tag;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.kiwi.util.tuple.immutable.Pair;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.LmlSyntax;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** Common base for macro tags.
 *
 * @author MJ */
public abstract class AbstractMacroLmlTag extends AbstractLmlTag {
    public AbstractMacroLmlTag(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    public Actor getActor() {
        return getParent() == null ? null : getParent().getActor();
    }

    @Override
    protected boolean supportsNamedAttributes() {
        return false;
    }

    /** @param content will have the arguments replaced.
     * @param macroArguments map of private macro arguments. This should be a separate map than these managed by LML
     *            data container, as regular LML arguments should not be parsed directly by the macro - marco replaces
     *            only its own arguments.
     * @return content with replaced arguments. */
    protected String replaceArguments(final String content, final ObjectMap<String, String> macroArguments) {
        final StringBuilder contentBuilder = new StringBuilder(content.length());
        final StringBuilder argumentNameBuilder = new StringBuilder();
        final LmlSyntax syntax = getParser().getSyntax();
        MAIN_LOOP:
        for (int index = 0, length = content.length(); index < length; index++) {
            final char character = content.charAt(index);
            if (character == syntax.getArgumentOpening()) {
                Strings.clearBuilder(argumentNameBuilder);
                // Character is an argument opening. It might be a regular LML parser argument - which is not ours to
                // replace - but it might also be a macro argument, so we need to investigate.
                for (int argumentIndex = index + 1; argumentIndex < length; argumentIndex++) {
                    final char argumentCharacter = content.charAt(argumentIndex);
                    if (argumentCharacter == syntax.getArgumentClosing()) {
                        final String argumentName = argumentNameBuilder.toString();
                        if (macroArguments.containsKey(argumentName)) {
                            // Argument name found and present in macro argument. Appending argument value:
                            contentBuilder.append(macroArguments.get(argumentName));
                            // Skipping argument declaration, jumping to index after argument closing marker:
                            index = argumentIndex;
                            continue MAIN_LOOP;
                        }
                    }
                    argumentNameBuilder.append(argumentCharacter);
                }
            }
            contentBuilder.append(character);
        }
        return contentBuilder.toString();
    }

    /** @param content will be split into two non-null strings according to the passed separator. If the separator does
     *            not occur in the content, first returned string will be the whole content and second will be an empty
     *            string. Cannot be null.
     * @param separator first occurrence of this separator will be stripped and will separate the content into 2 parts.
     *            Cannot be null.
     * @return content separated into 2 parts. */
    protected Pair<String, String> splitInTwo(final String content, final String separator) {
        int correctIndexes = 0;
        for (int index = 0, length = content.length(); index < length; index++) {
            if (Strings.compareIgnoreCase(content.charAt(index), separator.charAt(correctIndexes))) {
                if (++correctIndexes == separator.length()) {
                    return Pair.of(content.substring(0, index + 1 - separator.length()),
                            content.substring(index + 1, length));
                }
            } else {
                correctIndexes = 0;
            }
        }
        return Pair.of(content, Strings.EMPTY_STRING);
    }

    /** @param macroResult will be appended to the template reader. */
    protected void appendTextToParse(final String macroResult) {
        getParser().getTemplateReader().append(macroResult, "'" + getTagName() + "' macro result");
    }

    @Override
    public void handleChild(final LmlTag childTag) {
        // Should never happen.
        if (getParent() != null) {
            getParent().handleChild(childTag);
        }
    }

    @Override
    public void closeTag() {
        // Most macros will be done either in the constructor or plain text parsing method. Since macros usually do not
        // have to be finalized, this operation is considered optional, at best.
    }

    /** @return macro tag attributes converted to a single equation, with escaped characters properly converted. */
    protected String convertAttributesToEquation() {
        return Strings.merge((Object[]) getAttributes().toArray()).replace("&gt;", ">");
    }
}
