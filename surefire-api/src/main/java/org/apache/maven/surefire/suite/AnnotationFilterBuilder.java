package org.apache.maven.surefire.suite;

public class AnnotationFilterBuilder
{

    private interface ParserCallback
    {
        void onNewClass(Class clazz);
    }

    public AnnotationFilter build(String includedAnnotations, String excludedAnnotations)
    {
        // There is no a default way to log messages...
        //System.out.println("includedAnnotations: " + includedAnnotations);
        //System.out.println("excludedAnnotations: " + excludedAnnotations);
        final AnnotationFilter filter = new AnnotationFilter();

        parseClassNames(includedAnnotations, new ParserCallback()
        {
            public void onNewClass(Class clazz)
            {
                filter.accept(clazz);
            }
        });

        parseClassNames(excludedAnnotations, new ParserCallback()
        {
            public void onNewClass(Class clazz)
            {
                filter.reject(clazz);
            }
        });

        return filter;
    }

    private void parseClassNames(String classNames, ParserCallback callback)
    {
        if (classNames != null)
        {
            String[] names = classNames.split(":");
            for (int i = 0, namesLength = names.length; i < namesLength; i++)
            {
                String className = names[i];

                // There is no a default way to log messages...
                //System.out.println("Parsing class nam: " + className);
                try
                {
                    Class filterClass = getClass().getClassLoader().loadClass(className);
                    callback.onNewClass(filterClass);
                }
                catch (ClassNotFoundException e)
                {
                    // There is no a default way to log messages...
                    System.out.println("Ignoring test filter. Class not found" + className);
                }
            }
        }
    }
}
