package me.jume.ioc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

public class ApplicationContext {
	//容器使用Map 单例
	private Map<String,Object> single = new HashMap<String,Object>();
	//多实例的 容器 拿的时候再实例化 在内存中存储这些bean的配置
	private Map<String,Bean> mult = new HashMap<String,Bean>();
	/**
	 * 必须传递配置文件路径
	 * @param xmlPath
	 */
	public ApplicationContext(String xmlPath){
             init(xmlPath);
	}

	public void init(String xmlPath){
		//通过类路径加载配置文件到内存
		Document document = XmlUtil.getDocumentByInputStream(ApplicationContext.class.getResourceAsStream("/"+xmlPath));
		//拿到根节点 beans
		Element root = document.getRootElement();
		//拿到配置文件中所有的Bean节点
		List<Element> beans = XmlUtil.getChildElements(root, "bean");
		//增强for循环 迭代集合
		for(Element bean : beans){
			String beanName = bean.attributeValue("name");
			String beanClass = bean.attributeValue("class");
			String scope = bean.attributeValue("scope");

			//当时多例的时候（原型）只存储配置中原型再内存中
			if(scope!=null&&scope.equals("prototype")){
				Bean bea = null;
				bea = new Bean();
				bea.setName(beanName);
				bea.setClassName(beanClass);
				bea.setScope(scope);
				for(Object o : bean.selectNodes("property")){
					Element prop = (Element)o;
					String propName = prop.attributeValue("name");
					String propValue = prop.attributeValue("value");
					//创建属性对象，用来添加到bean集合
					Property pro = new Property();
					pro.setName(propName);
					pro.setValue(propValue);
					if(propValue==null){
						pro.setRef(prop.attributeValue("ref"));
					}
					bea.addProp(pro);
				}
				//从配置文件中得到的bean配置转换成java的实体对象 并且存储在原型容器中
				mult.put(beanName, bea);
			}else{//单例的情况下 容器初始化就要进行加载
				try {
					Class clazz = Class.forName(beanClass);
					//通过class的实例 实例化对象
					Object obj = clazz.newInstance();
					//对托管的bean对象实例进行赋值
					for(Object o : bean.selectNodes("property")){
						Element prop = (Element)o;
						String propName = prop.attributeValue("name");
						String propValue = prop.attributeValue("value");
						//拿到类声明的属性，而不是具备访问权限的那种
						Field  field = clazz.getDeclaredField(propName);
						//设置以后就可以对私有属性进行操作
						field.setAccessible(true);
						//创建属性对象，用来添加到bean集合
						if(propValue==null){
							//给刚刚创建的obj对当前的Field赋值
							//TODO 获取引用的bean对象 ref
							field.set(obj,getBean(prop.attributeValue("ref")));
						}else{
							field.set(obj, propValue);
						}
					}
					//把单例的实例放入到 单例bean容器
					single.put(beanName, obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


	public Object getBean(String beanName){
		//从单例中去拿
		Object obj = single.get(beanName);
		if(obj==null){//如果为空表示的是每次获取都要去实例化
			//从多例容器中拿出当前bean原型
			Bean b = mult.get(beanName);
			if(b==null){
				System.out.println("传入Bean没有配置到！！");
				return null;
			}
			try {
				Class clazz = Class.forName(b.getClassName());
				obj = clazz.newInstance();
				for(Property prop : b.getProps()){
					Field field = clazz.getDeclaredField(prop.getName());
					field.setAccessible(true);
					if(prop.getValue()==null){//依赖其他bean
						field.set(obj, getBean(prop.getRef()));
					}else{
						field.set(obj,prop.getValue());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return obj;
	}
}
