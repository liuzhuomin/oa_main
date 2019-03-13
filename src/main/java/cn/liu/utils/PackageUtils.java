package cn.liu.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.SystemPropertyUtils;

import cn.liu.annotions.ListenerAnnotion;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by cdliujian1 on 2016/1/13.
 * <li>包工具，根据package路径，加载class
 */
public class PackageUtils {

    private final static Log log = LogFactory.getLog(PackageUtils.class);
    //扫描  scanPackages 下的文件的匹配符
    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";


    /**
     * 结合spring的类扫描方式
     * 根据需要扫描的包路径及相应的注解，获取最终测method集合
     * 仅返回public方法，如果方法是非public类型的，不会被返回
     * 可以扫描工程下的class文件及jar中的class文件
     *
     * @param scanPackages
     * @param annotation
     * @return
     */
    public static Set<Method> findClassAnnotationMethods(String scanPackages, Class<? extends Annotation> annotation) {
        //获取所有的类
        Set<String> clazzSet = findPackageClass(scanPackages);
        Set<Method> methods = new HashSet<Method>();
        //遍历类，查询相应的annotation方法
        for (String clazz : clazzSet) {
            try {
                Set<Method> ms = findAnnotationMethods(clazz, annotation);
                if (ms != null) {
                    methods.addAll(ms);
                }
            } catch (ClassNotFoundException ignore) {
            }
        }
        return methods;
    }

    /**
     * 根据扫描包的,查询下面的所有类
     *
     * @param scanPackages 扫描的package路径
     * @return
     */
    public static Set<String> findPackageClass(String scanPackages) {
        if (scanPackages==null || scanPackages.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        //验证及排重包路径,避免父子路径多次扫描
        Set<String> packages = checkPackage(scanPackages);
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        Set<String> clazzSet = new HashSet<String>();
        for (String basePackage : packages) {
            if (basePackage==null || basePackage.isEmpty()) {
                continue;
            }
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    org.springframework.util.ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/" + DEFAULT_RESOURCE_PATTERN;
            try {
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    //检查resource，这里的resource都是class
                    String clazz = loadClassName(metadataReaderFactory, resource);
                    clazzSet.add(clazz);
                }
            } catch (Exception e) {
                log.error("获取包下面的类信息失败,package:" + basePackage, e);
            }

        }
        return clazzSet;
    }

    /**
     * 排重、检测package父子关系，避免多次扫描
     *
     * @param scanPackages
     * @return 返回检查后有效的路径集合
     */
    @SuppressWarnings("unchecked")
	private static Set<String> checkPackage(String scanPackages) {
        if (scanPackages==null || scanPackages.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        Set<String> packages = new HashSet<String>();
        //排重路径
        Collections.addAll(packages, scanPackages.split(","));
        for (String pInArr : packages.toArray(new String[packages.size()])) {
            if ((pInArr==null || pInArr.isEmpty()) || pInArr.equals(".") || pInArr.startsWith(".")) {
                continue;
            }
            if (pInArr.endsWith(".")) {
                pInArr = pInArr.substring(0, pInArr.length() - 1);
            }
            Iterator<String> packageIte = packages.iterator();
            boolean needAdd = true;
            while (packageIte.hasNext()) {
                String pack = packageIte.next();
                if (pInArr.startsWith(pack + ".")) {
                    //如果待加入的路径是已经加入的pack的子集，不加入
                    needAdd = false;
                } else if (pack.startsWith(pInArr + ".")) {
                    //如果待加入的路径是已经加入的pack的父集，删除已加入的pack
                    packageIte.remove();
                }
            }
            if (needAdd) {
                packages.add(pInArr);
            }
        }
        return packages;
    }


    /**
     * 加载资源，根据resource获取className
     *
     * @param metadataReaderFactory spring中用来读取resource为class的工具
     * @param resource              这里的资源就是一个Class
     * @throws IOException
     */
    private static String loadClassName(MetadataReaderFactory metadataReaderFactory, Resource resource) throws IOException {
        try {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (metadataReader != null) {
                    return metadataReader.getClassMetadata().getClassName();
                }
            }
        } catch (Exception e) {
            log.error("根据resource获取类名称失败", e);
        }
        return null;
    }

    /**
     * 把action下面的所有method遍历一次，标记他们是否需要进行敏感词验证
     * 如果需要，放入cache中
     *
     * @param fullClassName
     */
    public static Set<Method> findAnnotationMethods(String fullClassName, Class<? extends Annotation> anno) throws ClassNotFoundException {
        Set<Method> methodSet = new HashSet<Method>();
        Class<?> clz = Class.forName(fullClassName);
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getModifiers() != Modifier.PUBLIC) {
                continue;
            }
            Annotation annotation = method.getAnnotation(anno);
            if (annotation != null) {
                methodSet.add(method);
            }
        }
        return methodSet;
    }

    
    /**
     *<li> 获得包下面的所有的class
     * 
     * @param pack
     *            package完整名称
     * @param 
     * 		classzJoin 需要哪些注解类型的类
     * @return List包含所有class的实例
     */
    @SuppressWarnings("rawtypes")
	public static List<Class> getClasssFromPackage(String pack,List<Class<ListenerAnnotion>> classzJoin) {
      List<Class> clazzs = new ArrayList<Class>();

      // 是否循环搜索子包
      boolean recursive = true;

      // 包名字
      String packageName = pack;
      // 包名对应的路径名称
      String packageDirName = packageName.replace('.', '/');

      Enumeration<URL> dirs;

      try {
        dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        while (dirs.hasMoreElements()) {
          URL url = dirs.nextElement();

          String protocol = url.getProtocol();

          if ("file".equals(protocol)) {
            String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
            findClassInPackageByFile(packageName, filePath, recursive, clazzs,classzJoin);
          } else if ("jar".equals(protocol)) { }
        }

      } catch (Exception e) {
        e.printStackTrace();
      }

      return clazzs;
    }

    /**
     * <li>在package对应的路径下找到所有的class
     * 
     * @param packageName
     *            package名称
     * @param filePath
     *            package对应的路径
     * @param recursive
     *            是否查找子package
     * @param clazzs
     *            找到class以后存放的集合
     */
	public static void findClassInPackageByFile(String packageName, String filePath, final boolean recursive, @SuppressWarnings("rawtypes") List<Class> clazzs,List<Class<ListenerAnnotion>> classzJoin) {
      File dir = new File(filePath);
      if (!dir.exists() || !dir.isDirectory()) {
        return;
      }
      // 在给定的目录下找到所有的文件，并且进行条件过滤
      File[] dirFiles = dir.listFiles(new FileFilter() {

        @Override
        public boolean accept(File file) {
          boolean acceptDir = recursive && file.isDirectory();// 接受dir目录
          boolean acceptClass = file.getName().endsWith("class");// 接受class文件
          return acceptDir || acceptClass;
        }
      });

      for (File file : dirFiles) {
        if (file.isDirectory()) {
          findClassInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, clazzs,classzJoin);
        } else {
          String className = file.getName().substring(0, file.getName().length() - 6);
          try {
        	  Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
        	 if(!org.springframework.util.CollectionUtils.isEmpty(classzJoin)) {
        		 int i = 0;
        		 for (; i < classzJoin.size(); i++) {
					if(loadClass.isAnnotationPresent(classzJoin.get(i))) {
						break;
					}
				 }
        		 if(i ==classzJoin.size()) {
        			 continue;
        		 }
        	 }
            clazzs.add(loadClass);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

}