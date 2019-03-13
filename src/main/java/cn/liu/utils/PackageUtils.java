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
 * <li>�����ߣ�����package·��������class
 */
public class PackageUtils {

    private final static Log log = LogFactory.getLog(PackageUtils.class);
    //ɨ��  scanPackages �µ��ļ���ƥ���
    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";


    /**
     * ���spring����ɨ�跽ʽ
     * ������Ҫɨ��İ�·������Ӧ��ע�⣬��ȡ���ղ�method����
     * ������public��������������Ƿ�public���͵ģ����ᱻ����
     * ����ɨ�蹤���µ�class�ļ���jar�е�class�ļ�
     *
     * @param scanPackages
     * @param annotation
     * @return
     */
    public static Set<Method> findClassAnnotationMethods(String scanPackages, Class<? extends Annotation> annotation) {
        //��ȡ���е���
        Set<String> clazzSet = findPackageClass(scanPackages);
        Set<Method> methods = new HashSet<Method>();
        //�����࣬��ѯ��Ӧ��annotation����
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
     * ����ɨ�����,��ѯ�����������
     *
     * @param scanPackages ɨ���package·��
     * @return
     */
    public static Set<String> findPackageClass(String scanPackages) {
        if (scanPackages==null || scanPackages.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        //��֤�����ذ�·��,���⸸��·�����ɨ��
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
                    //���resource�������resource����class
                    String clazz = loadClassName(metadataReaderFactory, resource);
                    clazzSet.add(clazz);
                }
            } catch (Exception e) {
                log.error("��ȡ�����������Ϣʧ��,package:" + basePackage, e);
            }

        }
        return clazzSet;
    }

    /**
     * ���ء����package���ӹ�ϵ��������ɨ��
     *
     * @param scanPackages
     * @return ���ؼ�����Ч��·������
     */
    @SuppressWarnings("unchecked")
	private static Set<String> checkPackage(String scanPackages) {
        if (scanPackages==null || scanPackages.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        Set<String> packages = new HashSet<String>();
        //����·��
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
                    //����������·�����Ѿ������pack���Ӽ���������
                    needAdd = false;
                } else if (pack.startsWith(pInArr + ".")) {
                    //����������·�����Ѿ������pack�ĸ�����ɾ���Ѽ����pack
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
     * ������Դ������resource��ȡclassName
     *
     * @param metadataReaderFactory spring��������ȡresourceΪclass�Ĺ���
     * @param resource              �������Դ����һ��Class
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
            log.error("����resource��ȡ������ʧ��", e);
        }
        return null;
    }

    /**
     * ��action���������method����һ�Σ���������Ƿ���Ҫ�������д���֤
     * �����Ҫ������cache��
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
     *<li> ��ð���������е�class
     * 
     * @param pack
     *            package��������
     * @param 
     * 		classzJoin ��Ҫ��Щע�����͵���
     * @return List��������class��ʵ��
     */
    @SuppressWarnings("rawtypes")
	public static List<Class> getClasssFromPackage(String pack,List<Class<ListenerAnnotion>> classzJoin) {
      List<Class> clazzs = new ArrayList<Class>();

      // �Ƿ�ѭ�������Ӱ�
      boolean recursive = true;

      // ������
      String packageName = pack;
      // ������Ӧ��·������
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
     * <li>��package��Ӧ��·�����ҵ����е�class
     * 
     * @param packageName
     *            package����
     * @param filePath
     *            package��Ӧ��·��
     * @param recursive
     *            �Ƿ������package
     * @param clazzs
     *            �ҵ�class�Ժ��ŵļ���
     */
	public static void findClassInPackageByFile(String packageName, String filePath, final boolean recursive, @SuppressWarnings("rawtypes") List<Class> clazzs,List<Class<ListenerAnnotion>> classzJoin) {
      File dir = new File(filePath);
      if (!dir.exists() || !dir.isDirectory()) {
        return;
      }
      // �ڸ�����Ŀ¼���ҵ����е��ļ������ҽ�����������
      File[] dirFiles = dir.listFiles(new FileFilter() {

        @Override
        public boolean accept(File file) {
          boolean acceptDir = recursive && file.isDirectory();// ����dirĿ¼
          boolean acceptClass = file.getName().endsWith("class");// ����class�ļ�
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