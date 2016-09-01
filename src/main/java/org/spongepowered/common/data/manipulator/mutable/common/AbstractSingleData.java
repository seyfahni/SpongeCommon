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
package org.spongepowered.common.data.manipulator.mutable.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

/**
 * An abstraction for the various {@link DataManipulator}s that handle a single
 * value, adding the provided {@link #getValue()} and {@link #setValue(Object)}
 * methods for easy manipulation. This as well simplifies handling various
 * other common implementations, such as {@link #hashCode()} and
 * {@link #equals(Object)}.
 *
 * @param <T> The type of single value this will hold
 * @param <M> The type of {@link DataManipulator}
 * @param <I> The type of {@link ImmutableDataManipulator}
 */
@SuppressWarnings("unchecked")
public abstract class AbstractSingleData<T, M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
        extends AbstractData<M, I> {

    protected final Key<? extends BaseValue<T>> usedKey;
    private T value;

    protected AbstractSingleData(Class<M> manipulatorClass, T value, Key<? extends BaseValue<T>> usedKey) {
        super(manipulatorClass);
        this.value = checkNotNull(value);
        this.usedKey = checkNotNull(usedKey);
        registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(this.usedKey, AbstractSingleData.this::getValue);
        registerFieldSetter(this.usedKey, this::setValue);
        registerKeyValue(this.usedKey, AbstractSingleData.this::getValueGetter);
    }

    protected abstract Value<?> getValueGetter();

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        // we can delegate this since we have a direct value check as this is
        // a Single value.
        return key == this.usedKey ? Optional.of((E) this.value) : super.get(key);
    }

    @Override
    public boolean supports(Key<?> key) {
        return checkNotNull(key) == this.usedKey;
    }

    // We have to have this abstract to properly override for generics.
    @Override
    public abstract I asImmutable();

    protected T getValue() {
        return this.value;
    }

    protected M setValue(T value) {
        this.value = checkNotNull(value);
        // double casting due to jdk 6 type inference
        return (M) this;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(this.value);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final AbstractSingleData other = (AbstractSingleData) obj;
        return Objects.equal(this.value, other.value);
    }
}
