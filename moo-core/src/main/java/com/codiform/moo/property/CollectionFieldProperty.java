package com.codiform.moo.property;

import java.lang.reflect.Field;

import com.codiform.moo.GetPropertyException;
import com.codiform.moo.SetPropertyException;

public class CollectionFieldProperty extends AbstractCollectionProperty {

	private Field field;
	private String name;
	private String expression;
	private boolean explicit;
	private boolean ignore;

	public CollectionFieldProperty(Field field,
			com.codiform.moo.annotation.CollectionProperty annotation,
			String name, String expression,
			boolean explicit, boolean ignore) {
		super( annotation );
		this.field = field;
		this.name = name;
		this.expression = expression;
		this.explicit = explicit;
		this.ignore = ignore;

		if( !field.isAccessible() )
			field.setAccessible( true );
	}

	public String getName() {
		return name;
	}

	public String getSourcePropertyExpression() {
		return expression;
	}

	public Class<?> getType() {
		return field.getType();
	}

	public void setValue(Object instance, Object value) {
		checkValue( value );
		try {
			field.set( instance, value );
		} catch( IllegalArgumentException exception ) {
			throw new SetPropertyException( getName(), getType(), value,
					exception );
		} catch( IllegalAccessException exception ) {
			throw new SetPropertyException( getName(), getType(), value,
					exception );
		}
	}

	public boolean canSupportNull() {
		return true;
	}

	public Class<?> getDeclaringClass() {
		return field.getDeclaringClass();
	}

	public boolean isExplicit() {
		return explicit;
	}

	@Override
	public boolean isIgnored() {
		return ignore;
	}

	@Override
	public boolean canGetValue() {
		return true;
	}

	@Override
	public Object getValue(Object instance) {
		try {
			return field.get( instance );
		} catch( IllegalArgumentException exception ) {
			throw new GetPropertyException( getName(), getType(), exception );
		} catch( IllegalAccessException exception ) {
			throw new GetPropertyException( getName(), getType(), exception );
		}
	}

}
