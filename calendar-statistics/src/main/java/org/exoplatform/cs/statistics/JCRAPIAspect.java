/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.cs.statistics;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.statistics.JCRStatisticsManager;
import org.exoplatform.services.jcr.statistics.Statistics;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * This aspect is used to collect all the statistics of all the methods of the JCR API.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 26 mars 2010  
 */
@Aspect
public abstract class JCRAPIAspect
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger(JCRAPIAspect.class);
   
   /**
    * The result of the mapping if the corresponding value cannot be found.
    */
   private static final Statistics UNKNOWN = new Statistics(null, "?");

   /**
    * The flag that indicates if the aspect is initialized or not.
    */
   private static volatile boolean INITIALIZED;

   /**
    * The list of all the interfaces for which we want statistics
    */
   private static Class<?>[] TARGET_INTERFACES;

   /**
    * The mapping between the AspectJ signature and the target statistics
    */
   private static volatile Map<String, Statistics> MAPPING = Collections.unmodifiableMap(new HashMap<String, Statistics>());

   /**
    * The list of all the statistics, one per method
    */
   private final static Map<String, Map<String, Statistics>> ALL_STATISTICS = new HashMap<String, Map<String, Statistics>>();

   /**
    * Gives the name of the statistics from the given {@link Method} 
    */
   private static String getStatisticsName(Method m)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(m.getName());
      sb.append('(');
      Class<?>[] types = m.getParameterTypes();
      if (types != null)
      {
         boolean first = true;
         for (Class<?> type : types)
         {
            if (first)
            {
               first = false;
            }
            else
            {
               sb.append(", ");
            }
            sb.append(type.getSimpleName());
         }
      }
      sb.append(')');
      return sb.toString();
   }

   /**
    * Gives the corresponding statistics for the given target class and AspectJ signature
    * @param target the target {@link Class}
    * @param signature the AspectJ signature
    * @return the related {@link Statistics} or <code>null</code> if it cannot be found
    */
   private static Statistics getStatistics(Class<?> target, String signature)
   {
      initIfNeeded();
      Statistics statistics = MAPPING.get(signature);
      if (statistics == null)
      {
         synchronized (JCRAPIAspect.class)
         {
            Class<?> interfaceClass = findInterface(target);
            if (interfaceClass != null)
            {
               Map<String, Statistics> allStatistics = ALL_STATISTICS.get(interfaceClass.getSimpleName());
               if (allStatistics != null)
               {
                  int index1 = signature.indexOf('(');
                  int index = signature.substring(0, index1).lastIndexOf('.');
                  String name = signature.substring(index + 1);
                  statistics = allStatistics.get(name);
               }
            }
            if (statistics == null)
            {
               statistics = UNKNOWN;
            }
            Map<String, Statistics> tempMapping = new HashMap<String, Statistics>(MAPPING);
            tempMapping.put(signature, statistics);
            MAPPING = Collections.unmodifiableMap(tempMapping);            
         }
      }
      if (UNKNOWN.equals(statistics))
      {
         return null;
      }
      return statistics;
   }

   /**
    * Find the monitored interface from the target {@link Class}
    * @param target the target {@link Class}
    * @return the monitored interface, <code>null</code> otherwise
    */
   private static Class<?> findInterface(Class<?> target)
   {
      if (target == null)
      {
         return null;
      }
      Class<?>[] interfaces = target.getInterfaces();
      if (interfaces != null)
      {
         for (Class<?> c : TARGET_INTERFACES)
         {
            for (Class<?> i : interfaces)
            {
               if (c.getName().equals(i.getName()))
               {
                  return c;
               }
            }
         }
      }
      return findInterface(target.getSuperclass());
   }

   /**
    * Initializes the aspect if needed
    */
   private static void initIfNeeded()
   {
      if (!INITIALIZED)
      {
         synchronized (JCRAPIAspect.class)
         {
            if (!INITIALIZED)
            {
               ExoContainer container = ExoContainerContext.getTopContainer();
               JCRAPIAspectConfig config = null;
               if (container != null)
               {
                  config = (JCRAPIAspectConfig)container.getComponentInstanceOfType(JCRAPIAspectConfig.class);
               }
               if (config == null)
               {
                  TARGET_INTERFACES = new Class<?>[]{};
                  LOG.warn("No interface to monitor could be found");
               }
               else
               {
                  TARGET_INTERFACES = config.getTargetInterfaces();
                  for (Class<?> c : TARGET_INTERFACES)
                  {
                     Statistics global = new Statistics(null, "global");
                     Map<String, Statistics> statistics = new TreeMap<String, Statistics>();
                     Method[] methods = c.getMethods();
                     for (Method m : methods)
                     {
                        String name = getStatisticsName(m);
                        statistics.put(name, new Statistics(global, name));
                     }
                     JCRStatisticsManager.registerStatistics(c.getSimpleName(), global, statistics);
                     ALL_STATISTICS.put(c.getSimpleName(), statistics);
                  }
               }
               INITIALIZED = true;
            }
         }
      }
   }

   @Pointcut
   abstract void JCRAPIPointcut();

   @Before("JCRAPIPointcut()")
   public void begin(JoinPoint thisJoinPoint)
   {
      Statistics statistics =
         getStatistics(thisJoinPoint.getTarget().getClass(), thisJoinPoint.getSignature().toString());
      if (statistics != null)
      {
         statistics.begin();
      }
   }

   @After("JCRAPIPointcut()")
   public void end(JoinPoint thisJoinPoint)
   {
      Statistics statistics =
         getStatistics(thisJoinPoint.getTarget().getClass(), thisJoinPoint.getSignature().toString());
      if (statistics != null)
      {
         statistics.end();
      }
   }
}
