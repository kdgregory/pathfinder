// Copyright (c) Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.pathfinder.spring.context;

import org.apache.bcel.classfile.JavaClass;

import com.kdgregory.bcelx.classfile.Annotation;
import com.kdgregory.bcelx.classfile.Annotation.ParamValue;
import com.kdgregory.bcelx.parser.AnnotationParser;


/**
 *  Holds information for a bean selected as part of a classpath scan.
 */
public class ScannedBeanDefinition
extends BeanDefinition
{
    private JavaClass parsedClass;
    private AnnotationParser annos;


    // note: the AnnotationParser is passed as a premature optimization:
    //       since it's needed to extract the bean ID when calling super(),
    //       we'll let the caller create it so we only parse once
    public ScannedBeanDefinition(JavaClass klass, AnnotationParser ap)
    {
        super(DefinitionType.SCAN, extractBeanId(klass, ap), "", extractBeanClass(klass));
        parsedClass = klass;
        annos = ap;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the BCEL-parsed class representation.
     */
    public JavaClass getParsedClass()
    {
        return parsedClass;
    }


    /**
     *  Returns the annotation parser wrapping this class, for use in examining
     *  annotations on the class and its components.
     */
    public AnnotationParser getAnnotationParser()
    {
        return annos;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private static String extractBeanId(JavaClass klass, AnnotationParser ap)
    {
        Annotation anno = ap.getClassAnnotation(SpringConstants.ANNO_CONTROLLER);
        if (anno == null)
            anno = ap.getClassAnnotation(SpringConstants.ANNO_COMPONENT);
        if (anno == null)
            return null;

        ParamValue id = anno.getValue();
        if ((id != null) && (id.asScalar() != null))
            return String.valueOf(id.asScalar());

        return null;
    }


    private static String extractBeanClass(JavaClass klass)
    {
        return klass.getClassName();
    }
}
