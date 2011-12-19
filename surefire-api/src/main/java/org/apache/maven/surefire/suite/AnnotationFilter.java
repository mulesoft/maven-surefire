package org.apache.maven.surefire.suite;


import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

public class AnnotationFilter
{

    private List acceptedClasses = new LinkedList();
    private List rejectedClasses = new LinkedList();

    private void checkValidAnnotation(Class clazz)
    {
        if (!clazz.isAnnotation())
        {
            // There is no a default way to log messages...
            //System.out.println("Class is not an annotation: " + clazz.getName());
            throw new IllegalArgumentException("Class is not an annotation: " + clazz.getName());
        }
    }

    public void accept(Class clazz)
    {
        checkValidAnnotation(clazz);
        acceptedClasses.add(clazz);
    }

    public void reject(Class clazz)
    {
        checkValidAnnotation(clazz);
        rejectedClasses.add(clazz);
    }

    public List filter(Class[] source)
    {
        List result = new LinkedList();

        for (int i = 0, sourceLength = source.length; i < sourceLength; i++)
        {
            Class sourceClass = source[i];
            // There is no a default way to log messages...
            //System.out.println("Scanning class: " + sourceClass.getName());
            if (isClassAccepted(sourceClass) && !isClassRejected(sourceClass))
            {
                result.add(sourceClass);
            }
        }

        return result;
    }

    private boolean isClassAccepted(Class sourceClass)
    {
        boolean b = acceptedClasses.isEmpty() || containsSome(sourceClass, acceptedClasses);
        // There is no a default way to log messages...
        //System.out.println("Accepted " + sourceClass + ": " + b);
        return b;
    }

    private boolean isClassRejected(Class sourceClass)
    {
        boolean b = !rejectedClasses.isEmpty() && containsSome(sourceClass, rejectedClasses);
        // There is no a default way to log messages...
        //System.out.println("Rejected " + sourceClass + ": " + b);

        return b;
    }

    private boolean containsSome(Class sourceClass, List annotations)
    {
        Annotation[] annotations1 = sourceClass.getAnnotations();
        for (int i1 = 0, annotations1Length = annotations1.length; i1 < annotations1Length; i1++)
        {
            Annotation annotation = annotations1[i1];
            // There is no a default way to log messages...
            //System.out.println("Checking annotation: " + annotation);

            for (int i = 0, annotationsSize = annotations.size(); i < annotationsSize; i++)
            {
                Object filterAnnotation = annotations.get(i);
                if (((Class) filterAnnotation).isAssignableFrom(annotation.getClass()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public List getAcceptedClasses()
    {
        return acceptedClasses;
    }

    public List getRejectedClasses()
    {
        return rejectedClasses;
    }
}
