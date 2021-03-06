package com.github.czyzby.lml.parser.impl.tag.macro;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.impl.tag.AbstractActorLmlTag;
import com.github.czyzby.lml.parser.impl.tag.AbstractMacroLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;
import com.github.czyzby.lml.util.LmlUtilities;

/** Allows to register new tags from within LML templates. Normally, you have to implement {@code LmlTag} interface
 * (there are good abstracts for that - {@link AbstractMacroLmlTag} and {@link AbstractActorLmlTag}, but still) and add
 * a {@link LmlTagProvider} to syntax object - all in Java. This macro allows you to use LML templates to register new
 * tags; this is much less flexible solution, but a lot quicker, if you want to override a method or two in the original
 * class. For example, let's say you want a Table that does some extra work in the constructor:
 *
 * <blockquote>
 *
 * <pre>
 * public Table getMyTable() {
 *     return new Table() {
 *         {
 *             columnDefaults(1).pad(4f);
 *         }
 *     };
 * }
 * </pre>
 *
 * </blockquote>If you want to use this specialized Table method as a provider for a new tag, all you have to do is use
 * this macro:
 *
 * <blockquote>
 *
 * <pre>
 * &lt;@newTag myTable getMyTable&gt;
 * &lt;!-- Now you can use: --&gt;
 * &lt;myTable&gt;
 *      &lt;label pad=3&gt;Will be properly added.&lt;/label&gt;
 * &lt;/myTable&gt;
 * </pre>
 *
 * </blockquote>The first argument is an array of tag names (in this case: "myTable" tag name). The second is the method
 * reference that returns the actor instance that you want to assign to the tag names. If the actor extends
 * {@link Group}, {@link Table} or {@link Tree}, it will append children and (text converted to labels) with the most
 * appropriate method. Actor's attributes will be properly handled: if the actor extends a Table, for example, it will
 * be able to parse all Table attributes and its children can have any Cell attributes, as expected.
 *
 * <p>
 * But there are times when you need more data to create the widget, like its style. That's why you can create a method
 * that consumes {@link LmlActorBuilder}: <blockquote>
 *
 * <pre>
 * public Table getMyTable(LmlActorBuilder builder) {
 *     return new Table(lmlParser.getData().getSkin(builder.getSkinName()) {
 *         {
 *             columnDefaults(1).pad(4f);
 *         }
 *     };
 * }
 * </pre>
 *
 * </blockquote>You do need a reference to your LmlParser if you want to have multiple skins support, but this should
 * not be an issue. If you need a different builder (one with more data and more assigned attributes - be careful
 * though, as you might need to register some building attributes), you can pass a third macro argument: builder
 * provider method.
 *
 * <blockquote>
 *
 * <pre>
 * public LmlActorBuilder getMyBuilder() {
 *     return new TextLmlActorBuilder();
 * }
 *
 * public Table getMyTable(LmlActorBuilder builder) {
 *     return new Table(lmlParser.getData().getSkin(builder.getSkinName()) {
 *         {
 *             add(((TextLmlActorBuilder) builder).getText();
 *             columnDefaults(1).pad(4f);
 *         }
 *     };
 * }
 *
 * &lt;!-- In template: --&gt;
 * &lt;@newTag myTable;myAlias getMyTable getMyBuilder&gt;
 * </pre>
 *
 * </blockquote>Building attributes are mapped to builder types, so if you use one of custom widget builders (like the
 * text actor builder in the example above), your tag will automatically handle all its attributes.
 *
 * @author MJ */
public class NewTagLmlMacroTag extends AbstractMacroLmlTag {
    public NewTagLmlMacroTag(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    public void handleDataBetweenTags(final String rawData) {
    }

    @Override
    @SuppressWarnings("unchecked") // Casting actions, the usual stuff.
    public void closeTag() {
        final Array<String> attributes = getAttributes();
        if (GdxArrays.sizeOf(attributes) < 2) {
            getParser().throwError(
                    "Cannot register a new tag without two attributes: tag names array and method ID (consuming LmlActorBuilder, providing Actor).");
        }
        // Possible tag names:
        final String[] tagNames = getParser().parseArray(attributes.first(), getActor());
        // Creates actual actor:
        final ActorConsumer<Actor, LmlActorBuilder> creator = (ActorConsumer<Actor, LmlActorBuilder>) getParser()
                .parseAction(attributes.get(1), new LmlActorBuilder());
        final ActorConsumer<LmlActorBuilder, Actor> builderCreator;
        if (attributes.size > 2) { // Provides builders:
            builderCreator = (ActorConsumer<LmlActorBuilder, Actor>) getParser().parseAction(attributes.get(2),
                    getActor());
        } else { // Using default builders:
            builderCreator = null;
        }
        if (creator == null) { // No actor creation method - new tag cannot be created.
            getParser().throwError(
                    "Cannot register a method consuming LmlActorBuilder, providing Actor. Method consuming LmlActorBuilder and returning actor not found for attribute: "
                            + attributes.get(1));

        }
        // Registering provider that will create custom tags for the selected tag names:
        getParser().getSyntax().addTagProvider(getNewTagProvider(creator, builderCreator), tagNames);
    }

    /** @param creator method that spawns new actors.
     * @param builderCreator spawns actor builders. Optional.
     * @return an instance of {@link LmlTagProvider} that provides custom tags. */
    protected LmlTagProvider getNewTagProvider(final ActorConsumer<Actor, LmlActorBuilder> creator,
            final ActorConsumer<LmlActorBuilder, Actor> builderCreator) {
        return new LmlTagProvider() {
            @Override
            public LmlTag create(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
                return new CustomLmlTag(parser, parentTag, rawTagData) {
                    @Override
                    protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
                        return creator.consume(builder);
                        // This is an abstract method, because we cannot pass the creator in the constructor. The actor
                        // is created IN the super constructor (so it can be final), before creator is even assigned.
                    }

                    @Override
                    protected LmlActorBuilder getNewInstanceOfBuilder() {
                        if (builderCreator != null) {
                            return builderCreator.consume(null);
                        }
                        return super.getNewInstanceOfBuilder();
                    }
                };
            }
        };
    }

    /** Custom tag created with new tag macro. If the returned actor extends {@link Group}, it can be parental: plain
     * text will be converted to labels and added; regular tags will be added with {@link Group#addActor(Actor)}. If the
     * actor implements {@link Layout}, it will be packed after its tag is closed.
     *
     * @author MJ */
    public static abstract class CustomLmlTag extends AbstractActorLmlTag {
        public CustomLmlTag(final LmlParser parser, final LmlTag parentTag, final String rawTagData) {
            super(parser, parentTag, rawTagData);
        }

        @Override
        protected void handlePlainTextLine(final String plainTextLine) {
            if (getActor() instanceof Label) {
                // Labels might be a pretty basic widget that sometimes needs extension, so we want to support its
                // unique text parsing.
                appendText((Label) getActor(), plainTextLine);
            } else if (getActor() instanceof Group) {
                addChild(toLabel(plainTextLine));
            }
        }

        /** @param actor casted for convenience.
         * @param plainTextLine should be appended to label. */
        protected void appendText(final Label actor, final String plainTextLine) {
            final String textToAppend = getParser().parseString(plainTextLine, actor);
            if (Strings.isEmpty(actor.getText())) {
                actor.setText(textToAppend);
            } else {
                if (LmlUtilities.isMultiline(actor)) {
                    actor.getText().append('\n');
                }
                actor.getText().append(textToAppend);
            }
            actor.invalidate();
        }

        /** @param child will be added to the actor casted to a Group or a Table. */
        protected void addChild(final Actor child) {
            final Actor actor = getActor();
            if (actor instanceof Tree) {
                final Tree.Node node = LmlUtilities.getTreeNode(child);
                if (node != null) {
                    ((Tree) actor).add(node);
                } else {
                    ((Tree) actor).add(new Tree.Node(child));
                }
            } else if (actor instanceof Table) {
                LmlUtilities.getCell(child, (Table) actor);
            } else {
                ((Group) actor).addActor(child);
            }
        }

        @Override
        protected void handleValidChild(final LmlTag childTag) {
            if (getActor() instanceof Group) {
                addChild(childTag.getActor());
            }
        }

        @Override
        protected void doOnTagClose() {
            if (getActor() instanceof Layout) {
                ((Layout) getActor()).pack();
            }
        }
    }
}
