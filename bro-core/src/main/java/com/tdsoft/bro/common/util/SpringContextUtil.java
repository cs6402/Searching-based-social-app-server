package com.tdsoft.bro.common.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextUtil implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;

	  /**
	   * 實現ApplicationContextAware介面的方法，設定applicationContext   
	   * @param applicationContext
	   * @throws BeansException
	   */
	  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	    SpringContextUtil.applicationContext = applicationContext;
	  }
	 
	  /**
	   * @return ApplicationContext
	   */
	  public static ApplicationContext getApplicationContext() {
	    return applicationContext;
	  }
	 
	  /**
	   * 取得Bean
	   * @param name 在Spring Context中設定的Bean id/name
	   * @return Object 相應的Bean物件
	   * @throws BeansException
	   */
	  public static Object getBean(String name) throws BeansException {
	    return applicationContext.getBean(name);
	  }
	 
	  /**
	   * 取得類型為requiredType的對象
	   * 如果bean不能被類型轉換，相應的異常將會被拋出(BeanNotOfRequiredTypeException)
	   * @param name 在Spring Context中設定的Bean id/name
	   * @param requiredType 返回對象類型
	   * @return Object 相應的Bean物件
	   * @throws BeansException
	   */
	  public static Object getBean(String name, Class<?> requiredType) throws BeansException {
	    return applicationContext.getBean(name, requiredType);
	  }
	 
	  /**
	   * 如果BeanFactory包含一個與所給名稱匹配的bean定義，則回傳true
	   * @param name 在Spring Context中設定的Bean id/name
	   * @return boolean
	   */
	  public static boolean containsBean(String name) {
	    return applicationContext.containsBean(name);
	  }
	 
	  /**
	   * 判斷以給定名字註冊的bean定義是singleton還是prototype
	   * 如果找不到bean，將會拋出異常NoSuchBeanDefinitionException
	   * @param name 在Spring Context中設定的Bean id/name
	   * @return boolean
	   * @throws NoSuchBeanDefinitionException
	   */
	  public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
	    return applicationContext.isSingleton(name);
	  }
	 
	  /**
	   * 取得註冊對象的類型
	   * @param name 在Spring Context中設定的Bean id/name
	   * @return Class 
	   * @throws NoSuchBeanDefinitionException
	   */
	  public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
	    return applicationContext.getType(name);
	  }
	 
	  /**
	   * 如果給定的bean名字在bean定義中有別名，則反回這些別名
	   * @param name 在Spring Context中設定的Bean id/name
	   * @return
	   * @throws NoSuchBeanDefinitionException
	   */
	  public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
	    return applicationContext.getAliases(name);
	  }
	  
}
