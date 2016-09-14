/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.api.text;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.interfaces.text.IMixinText;
import org.spongepowered.common.text.action.SpongeClickAction;
import org.spongepowered.common.text.action.SpongeHoverAction;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Optional;

@Mixin(value = Text.class, remap = false)
public abstract class MixinText implements IMixinText {

    @Shadow @Final protected TextFormat format;
    @Shadow @Final protected ImmutableList<Text> children;
    @Shadow @Final protected Optional<ClickAction<?>> clickAction;
    @Shadow @Final protected Optional<HoverAction<?>> hoverAction;
    @Shadow @Final protected Optional<ShiftClickAction<?>> shiftClickAction;

    private ITextComponent component;
    private String json;

    protected TextComponentBase createComponent() {
        throw new UnsupportedOperationException();
    }

    private ITextComponent initializeComponent() {
        if (this.component == null) {
            this.component = createComponent();
            Style style = this.component.getStyle();

            if (this.format.getColor() != TextColors.NONE) {
                style.setColor(((SpongeTextColor) this.format.getColor()).getHandle());
            }

            if (!this.format.getStyle().isEmpty()) {
                style.setBold(this.format.getStyle().isBold().orElse(null));
                style.setItalic(this.format.getStyle().isItalic().orElse(null));
                style.setUnderlined(this.format.getStyle().hasUnderline().orElse(null));
                style.setStrikethrough(this.format.getStyle().hasStrikethrough().orElse(null));
                style.setObfuscated(this.format.getStyle().isObfuscated().orElse(null));
            }

            if (this.clickAction.isPresent()) {
                style.setClickEvent(SpongeClickAction.getHandle(this.clickAction.get()));
            }

            if (this.hoverAction.isPresent()) {
                style.setHoverEvent(SpongeHoverAction.getHandle(this.hoverAction.get()));
            }

            if (this.shiftClickAction.isPresent()) {
                ShiftClickAction.InsertText insertion = (ShiftClickAction.InsertText) this.shiftClickAction.get();
                style.setInsertion(insertion.getResult());
            }

            for (Text child : this.children) {
                this.component.appendSibling(((IMixinText) child).toComponent());
            }
        }
        return this.component;
    }

    private ITextComponent getHandle() {
        return initializeComponent();
    }

    @Override
    public ITextComponent toComponent() {
        return getHandle().createCopy(); // Mutable instances are not nice :(
    }

    @Override
    public String toPlain() {
        return ((IMixinTextComponent) getHandle()).toPlain();
    }

    @Override
    public String toJson() {
        if (this.json == null) {
            this.json = ITextComponent.Serializer.componentToJson(getHandle());
        }

        return this.json;
    }

    @Override
    public String getLegacyFormatting() {
        return ((IMixinTextComponent) getHandle()).getLegacyFormatting();
    }

    @Override
    public String toLegacy(char code) {
        return ((IMixinTextComponent) getHandle()).toLegacy(code);
    }

}
