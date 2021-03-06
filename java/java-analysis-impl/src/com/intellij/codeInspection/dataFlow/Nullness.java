/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInspection.dataFlow;

import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author cdr
*/
public enum Nullness {
  NOT_NULL, NULLABLE, UNKNOWN;

  /**
   * Convert nullability annotation returned by {@link NullableNotNullManager#findEffectiveNullabilityAnnotation(PsiModifierListOwner)}
   * to {@code Nullness} value
   *
   * @param annotation annotation to convert
   * @return Nullness value
   */
  @NotNull
  public static Nullness fromAnnotation(@Nullable PsiAnnotation annotation) {
    if (annotation == null) return UNKNOWN;
    if (NullableNotNullManager.isNullableAnnotation(annotation) || NullableNotNullManager.isContainerNullableAnnotation(annotation)) {
      return NULLABLE;
    }
    if (NullableNotNullManager.isNotNullAnnotation(annotation) || NullableNotNullManager.isContainerNotNullAnnotation(annotation)) {
      return NOT_NULL;
    }
    return UNKNOWN;
  }
}
