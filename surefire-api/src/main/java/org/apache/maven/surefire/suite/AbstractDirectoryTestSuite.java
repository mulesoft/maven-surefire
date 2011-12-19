package org.apache.maven.surefire.suite;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.maven.surefire.Surefire;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.report.ReporterManagerFactory;
import org.apache.maven.surefire.testset.SurefireTestSet;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.SurefireDirectoryScanner;

public abstract class AbstractDirectoryTestSuite
    implements SurefireTestSuite
{
    private static final String ACCEPTED_ANNOTATIONS = "includedAnnotations";
    private static final String REJECTED_ANNOTATIONS = "excludedAnnotations";

    protected static ResourceBundle bundle = ResourceBundle.getBundle( Surefire.SUREFIRE_BUNDLE_NAME );

    protected Map testSets;

    private int totalTests;
    
    private final SurefireDirectoryScanner surefireDirectoryScanner;


    protected AbstractDirectoryTestSuite( File basedir, List includes, List excludes )
    {
        this.surefireDirectoryScanner = new SurefireDirectoryScanner(basedir, includes, excludes);
    }

    public Map locateTestSets( ClassLoader classLoader )
        throws TestSetFailedException
    {
        if ( testSets != null )
        {
            throw new IllegalStateException( "You can't call locateTestSets twice" );
        }
        testSets = new HashMap();

        Class[] locatedClasses = surefireDirectoryScanner.locateTestClasses( classLoader);

        Class[] filteredClasses = filterClassesByAnnotations(locatedClasses);

        for ( int i = 0; i < filteredClasses.length; i++ )
        {
            Class testClass = filteredClasses[i];
            SurefireTestSet testSet = createTestSet( testClass, classLoader );

                if ( testSet == null )
                {
                    continue;
                }

                if ( testSets.containsKey( testSet.getName() ) )
                {
                    throw new TestSetFailedException( "Duplicate test set '" + testSet.getName() + "'" );
                }
                testSets.put( testSet.getName(), testSet );

                totalTests++;
        }

        return Collections.unmodifiableMap( testSets );
    }

    private Class[] filterClassesByAnnotations(Class[] locatedClasses)
    {
        List filteredClasses;

        try
        {
            AnnotationFilterBuilder builder = new AnnotationFilterBuilder();
            AnnotationFilter filter = builder.build(System.getProperty(ACCEPTED_ANNOTATIONS), System.getProperty(REJECTED_ANNOTATIONS));

            filteredClasses = filter.filter(locatedClasses);
        }
        catch (Exception e) {
            System.out.println("MONCHO Error: " + e.getMessage());
            filteredClasses = new ArrayList();
        }

        return copyListToArray(filteredClasses);
    }

    private Class[] copyListToArray(List classesToFilter)
    {
        Class[] filteredClasses = new Class[classesToFilter.size()];
        for (int i =0; i <classesToFilter.size(); i++)
        {
            // There is no a default way to log messages...
            //System.out.println("Class: " + classesToFilter.get(i).getClass().getName());
            filteredClasses[i] = (Class) classesToFilter.get(i);
        }
        return filteredClasses;
    }

    protected abstract SurefireTestSet createTestSet( Class testClass, ClassLoader classLoader )
        throws TestSetFailedException;

    public void execute( ReporterManagerFactory reporterManagerFactory, ClassLoader classLoader )
        throws ReporterException, TestSetFailedException
    {
        if ( testSets == null )
        {
            throw new IllegalStateException( "You must call locateTestSets before calling execute" );
        }
        for ( Iterator i = testSets.values().iterator(); i.hasNext(); )
        {
            SurefireTestSet testSet = (SurefireTestSet) i.next();

            executeTestSet( testSet, reporterManagerFactory, classLoader );
        }
    }

    private void executeTestSet( SurefireTestSet testSet, ReporterManagerFactory reporterManagerFactory,
                                 ClassLoader classLoader )
        throws ReporterException, TestSetFailedException
    {

        ReporterManager reporterManager = reporterManagerFactory.createReporterManager();

        String rawString = bundle.getString( "testSetStarting" );

        ReportEntry report = new ReportEntry( this.getClass().getName(), testSet.getName(), rawString );

        reporterManager.testSetStarting( report );

        testSet.execute( reporterManager, classLoader );

        rawString = bundle.getString( "testSetCompletedNormally" );

        report = new ReportEntry( this.getClass().getName(), testSet.getName(), rawString );

        reporterManager.testSetCompleted( report );

        reporterManager.reset();
    }

    public void execute( String testSetName, ReporterManagerFactory reporterManagerFactory, ClassLoader classLoader )
        throws ReporterException, TestSetFailedException
    {
        if ( testSets == null )
        {
            throw new IllegalStateException( "You must call locateTestSets before calling execute" );
        }
        SurefireTestSet testSet = (SurefireTestSet) testSets.get( testSetName );

        if ( testSet == null )
        {
            throw new TestSetFailedException( "Unable to find test set '" + testSetName + "' in suite" );
        }

        executeTestSet( testSet, reporterManagerFactory, classLoader );
    }

    public int getNumTests()
    {
        if ( testSets == null )
        {
            throw new IllegalStateException( "You must call locateTestSets before calling getNumTests" );
        }
        return totalTests;
    }

}
