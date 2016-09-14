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
package org.spongepowered.common.text.serializer;

import com.google.gson.JsonParseException;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.interfaces.text.IMixinText;

/**
 * TextSerializer implementation for the json format.
 */
public final class JsonTextSerializer implements TextSerializer {

    @Override
    public String serialize(Text text) {
        return ((IMixinText) text).toJson();
    }

    @Override
    public Text deserialize(String input) throws TextParseException {
        try {
            return ((IMixinTextComponent) ITextComponent.Serializer.jsonToComponent(input)).toText();
        } catch (JsonParseException e) {
            throw new TextParseException("Failed to parse JSON", e);
        }
    }


}
