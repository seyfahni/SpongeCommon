package org.spongepowered.common.text;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.spongepowered.common.text.SpongeTexts.COLOR_CHAR;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.common.launch.LaunchWrapperTestRunner;

@RunWith(LaunchWrapperTestRunner.class)
public class LegacySerializerTest {

    @Test
    public void testPlainText() {
        assertThat(SpongeTexts.toLegacy(new TextComponentString("test")), is("test"));
    }

    @Test
    public void testTranslatableText() {
        assertThat(SpongeTexts.toLegacy(new TextComponentTranslation("test")), is("test"));
    }

    @Test
    public void testColoredText() {
        ITextComponent component = new TextComponentString("test");
        component.getStyle().setColor(TextFormatting.RED);
        assertThat(SpongeTexts.toLegacy(component), is(COLOR_CHAR + "ctest"));
    }

    @Test
    public void testNestedText() {
        ITextComponent component = new TextComponentString("first");
        component.getStyle().setColor(TextFormatting.RED);

        component.appendSibling(new TextComponentString("second"));

        TextComponentString component2 = new TextComponentString("third");
        component2.getStyle().setColor(TextFormatting.BLUE);
        component.appendSibling(component2);

        assertThat(SpongeTexts.toLegacy(component), is(COLOR_CHAR + "cfirstsecond" + COLOR_CHAR + "9third"));
    }

    @Test
    public void testEmptyTranslatableText() {
        assertThat(SpongeTexts.toLegacy(new TextComponentString("blah").appendSibling(new TextComponentTranslation(""))), is("blah"));
    }

}
