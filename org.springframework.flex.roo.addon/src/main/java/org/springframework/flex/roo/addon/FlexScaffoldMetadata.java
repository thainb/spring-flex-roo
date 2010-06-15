package org.springframework.flex.roo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.addon.beaninfo.BeanInfoMetadata;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.finder.FinderMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.DefaultMethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.itd.ItdSourceFileComposer;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

public class FlexScaffoldMetadata extends
		AbstractItdTypeDetailsProvidingMetadataItem {

	//private static Logger logger = Logger.getLogger(FlexScaffoldMetadata.class.getName());
	
	private static final String PROVIDES_TYPE_STRING = FlexScaffoldMetadata.class.getName();
	private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);
	
	private BeanInfoMetadata beanInfoMetadata;
	private EntityMetadata entityMetadata;
	private String entityReference;
	
	public FlexScaffoldMetadata(String identifier, JavaType aspectName,
			PhysicalTypeMetadata governorPhysicalTypeMetadata, BeanInfoMetadata beanInfoMetadata, EntityMetadata entityMetadata, FinderMetadata finderMetadata) {
		
		super(identifier, aspectName, governorPhysicalTypeMetadata);

		Assert.isTrue(isValid(identifier), "Metadata identification string '" + identifier + "' does not appear to be a valid");
		Assert.notNull(beanInfoMetadata, "Bean info metadata required");
		Assert.notNull(entityMetadata, "Entity metadata required");
		Assert.notNull(entityMetadata, "Finder metadata required");
		
		if (!isValid()) {
			return;
		}

		this.beanInfoMetadata = beanInfoMetadata;
		this.entityMetadata = entityMetadata;
		this.entityReference = StringUtils.uncapitalize(beanInfoMetadata.getJavaBean().getSimpleTypeName());
		
		builder.addMethod(getCreateMethod());
		builder.addMethod(getShowMethod());
		builder.addMethod(getListMethod());
		builder.addMethod(getListPagedMethod());
		builder.addMethod(getUpdateMethod());
		builder.addMethod(getRemoveMethod());
		
		itdTypeDetails = builder.build();
		
		new ItdSourceFileComposer(itdTypeDetails);
	}
	
	public static final String getMetadataIdentiferType() {
		return PROVIDES_TYPE;
	}

	public static final String createIdentifier(JavaType javaType, Path path) {
		return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType, path);
	}

	public static final JavaType getJavaType(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, metadataIdentificationString);
	}

	public BeanInfoMetadata getBeanInfoMetadata() {
		return beanInfoMetadata;
	}

	public EntityMetadata getEntityMetadata() {
		return entityMetadata;
	}

	public String getEntityReference() {
		return entityReference;
	}

	public static final Path getPath(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
	}
	
	public static boolean isValid(String metadataIdentificationString) {
		return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
	}
	
	private MethodMetadata getRemoveMethod() {
		JavaSymbolName methodName = new JavaSymbolName("remove");
		
		MethodMetadata method = methodExists(methodName);
		
		if (method != null) return method;
		
		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		paramTypes.add(new AnnotatedJavaType(entityMetadata.getIdentifierField().getFieldType(), null));	
		
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
		paramNames.add(new JavaSymbolName(entityMetadata.getIdentifierField().getFieldName().getSymbolName()));
		
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		if (!entityMetadata.getIdentifierField().getFieldType().isPrimitive()) {
			bodyBuilder.appendFormalLine("if (" + entityMetadata.getIdentifierField().getFieldName().getSymbolName() + " == null) throw new IllegalArgumentException(\"An Identifier is required\");");
		}
		
		bodyBuilder.appendFormalLine(beanInfoMetadata.getJavaBean().getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "." + entityMetadata.getFindMethod().getMethodName() + "(" + entityMetadata.getIdentifierField().getFieldName().getSymbolName() + ")." + entityMetadata.getRemoveMethod().getMethodName() + "();");
		
		
		return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE, paramTypes, paramNames, null, null, bodyBuilder.getOutput());
	}
	
	private MethodMetadata getListMethod() {
		JavaSymbolName methodName = new JavaSymbolName("list");		
		
		MethodMetadata method = methodExists(methodName);
		if (method != null) return method;
		
		List<JavaType> typeParams = new ArrayList<JavaType>();
		typeParams.add(beanInfoMetadata.getJavaBean());
		JavaType returnType = new JavaType("java.util.List", 0, DataType.TYPE, null, typeParams);
		
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder(); 		
		bodyBuilder.appendFormalLine("return " + beanInfoMetadata.getJavaBean().getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "." + entityMetadata.getFindAllMethod().getMethodName() + "();");
		return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName, returnType, null, null, null, null, bodyBuilder.getOutput());
	}
	
	private MethodMetadata getListPagedMethod() {
		JavaSymbolName methodName = new JavaSymbolName("listPaged");		
		
		MethodMetadata method = methodExists(methodName);
		if (method != null) return method;
		
		List<JavaType> typeParams = new ArrayList<JavaType>();
		typeParams.add(beanInfoMetadata.getJavaBean());
		JavaType returnType = new JavaType("java.util.List", 0, DataType.TYPE, null, typeParams);
			
		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		paramTypes.add(new AnnotatedJavaType(new JavaType("Integer"), null));
		paramTypes.add(new AnnotatedJavaType(new JavaType("Integer"), null));
		
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();		
		paramNames.add(new JavaSymbolName("page"));
		paramNames.add(new JavaSymbolName("size"));
		
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder(); 		
		bodyBuilder.appendFormalLine("if (page != null || size != null) {");
		bodyBuilder.indent();
		bodyBuilder.appendFormalLine("int sizeNo = size == null ? 10 : size.intValue();");
		bodyBuilder.appendFormalLine("return " + beanInfoMetadata.getJavaBean().getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "." + entityMetadata.getFindEntriesMethod().getMethodName() + "(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo);");
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("} else {");
		bodyBuilder.indent();
		bodyBuilder.appendFormalLine("return list();");
		bodyBuilder.indentRemove();
		bodyBuilder.appendFormalLine("}");

		return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName, returnType, paramTypes, paramNames, null, null, bodyBuilder.getOutput());
	}
	
	private MethodMetadata getShowMethod() {
		JavaSymbolName methodName = new JavaSymbolName("show");		
		
		MethodMetadata method = methodExists(methodName);
		if (method != null) return method;
				
		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		paramTypes.add(new AnnotatedJavaType(entityMetadata.getIdentifierField().getFieldType(), null));	
		
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
		paramNames.add(new JavaSymbolName(entityMetadata.getIdentifierField().getFieldName().getSymbolName()));
				
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		if (!entityMetadata.getIdentifierField().getFieldType().isPrimitive()) {
			bodyBuilder.appendFormalLine("if (" + entityMetadata.getIdentifierField().getFieldName().getSymbolName() + " == null) throw new IllegalArgumentException(\"An Identifier is required\");");
		}
		bodyBuilder.appendFormalLine("return "+ beanInfoMetadata.getJavaBean().getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver()) + "." + entityMetadata.getFindMethod().getMethodName() + "(" + entityMetadata.getIdentifierField().getFieldName().getSymbolName() + ");");

		return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName, beanInfoMetadata.getJavaBean(), paramTypes, paramNames, null, null, bodyBuilder.getOutput());
	}
	
	private MethodMetadata getCreateMethod() {		
		JavaSymbolName methodName = new JavaSymbolName("create");
		
		MethodMetadata method = methodExists(methodName);
		if (method != null) return method;
		
		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		paramTypes.add(new AnnotatedJavaType(beanInfoMetadata.getJavaBean(), null));
		
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
		paramNames.add(new JavaSymbolName(entityReference));
				
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine(entityReference + "." + entityMetadata.getPersistMethod().getMethodName() + "();");
		bodyBuilder.appendFormalLine("return "+entityReference+";");
		
		return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName, beanInfoMetadata.getJavaBean(), paramTypes, paramNames, null, null, bodyBuilder.getOutput());
	}
	
	private MethodMetadata getUpdateMethod() {		
		JavaSymbolName methodName = new JavaSymbolName("update");
		
		MethodMetadata method = methodExists(methodName);
		if (method != null) return method;

		List<AnnotatedJavaType> paramTypes = new ArrayList<AnnotatedJavaType>();
		paramTypes.add(new AnnotatedJavaType(beanInfoMetadata.getJavaBean(), null));	
	
		List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
		paramNames.add(new JavaSymbolName(entityReference));
				
		InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
		bodyBuilder.appendFormalLine("if (" + entityReference + " == null) throw new IllegalArgumentException(\"A " + entityReference+ " is required\");");
		bodyBuilder.appendFormalLine(entityReference + "." + entityMetadata.getMergeMethod().getMethodName() + "();");
		bodyBuilder.appendFormalLine("return "+entityReference+";");
		
		return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, methodName, beanInfoMetadata.getJavaBean(), paramTypes, paramNames, null, null, bodyBuilder.getOutput());
	}
	
	private MethodMetadata methodExists(JavaSymbolName methodName) {
		// We have no access to method parameter information, so we scan by name alone and treat any match as authoritative
		// We do not scan the superclass, as the caller is expected to know we'll only scan the current class
		for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
			if (method.getMethodName().equals(methodName)) {
				// Found a method of the expected name; we won't check method parameters though
				return method;
			}
		}
		return null;
	}

}
