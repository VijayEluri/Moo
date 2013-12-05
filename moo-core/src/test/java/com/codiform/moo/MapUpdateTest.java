package com.codiform.moo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.codiform.moo.annotation.MapProperty;
import com.codiform.moo.curry.Update;

public class MapUpdateTest {

	public static class Value {
		private Integer id;
		private String name;

		@SuppressWarnings("unused")
		private Value() {
			// for translation only
		}

		public Value(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

	public static class ValueDto {
		private Integer id;
		private String name;

		@SuppressWarnings("unused")
		private ValueDto() {
			// for translation only
		}

		public ValueDto(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

	public static class ValueDtoMap {
		private Map<String, ValueDto> values;

		public ValueDtoMap() {
			values = new HashMap<String, ValueDto>();
		}

		public ValueDto get(String index) {
			return values.get( index );
		}

		public Map<String, ValueDto> getValues() {
			return values;
		}

		public void put(String index, ValueDto dto) {
			values.put( index, dto );
		}
	}

	public static class ValueMap {
		@MapProperty(valueClass = Value.class, update = true)
		private Map<String, Value> values;

		public ValueMap() {
			values = new HashMap<String, Value>();
		}

		public boolean containsKey(String key) {
			return values.containsKey( key );
		}

		public Value get(String index) {
			return values.get( index );
		}

		public void put(String index, Value value) {
			values.put( index, value );
		}

		public int size() {
			return values.size();
		}
	}

	public static class RetainValueMap {
		@MapProperty(valueClass = Value.class, update = true, removeOrphans = false)
		private Map<String, Value> values;

		public RetainValueMap() {
			values = new HashMap<String, Value>();
		}

		public boolean containsKey(String key) {
			return values.containsKey( key );
		}

		public Value get(String index) {
			return values.get( index );
		}

		public void put(String index, Value value) {
			values.put( index, value );
		}

		public int size() {
			return values.size();
		}
	}

	private void assertEqualValues(ValueDto expectedValues, Value actual) {
		assertEquals( expectedValues.getId(), actual.getId() );
		assertEquals( expectedValues.getName(), actual.getName() );
	}

	private void assertIdentityAndValues(Value expectedIdentity,
			ValueDto expectedValues,
			Value actual) {
		assertSame( expectedIdentity, actual );
		assertEqualValues( expectedValues, actual );
	}

	@Test
	public void testUpdateMapPropertyUpdatesObjectsInMapByKey() {
		ValueDto flaDto = new ValueDto( 242, "Front Line Assembly" );
		ValueDto tkkDto = new ValueDto( 38, "My Life with the Thrill Kill Kult" );
		ValueDtoMap dtoMap = new ValueDtoMap();
		dtoMap.put( "FLA", flaDto );
		dtoMap.put( "TKK", tkkDto );

		Value fla = new Value( 1, "Front Line" );
		Value tkk = new Value( 2, "Thrill Kill" );
		ValueMap valueMap = new ValueMap();
		valueMap.put( "TKK", tkk );
		valueMap.put( "FLA", fla );

		Update.from( dtoMap ).to( valueMap );

		assertIdentityAndValues( tkk, tkkDto, valueMap.get( "TKK" ) );
		assertIdentityAndValues( fla, flaDto, valueMap.get( "FLA" ) );
	}

	@Test
	public void testUpdateMapWillInsertItemWhoseKeyIsNotPresentInDestinationMap() {
		ValueDto tardisDto = new ValueDto( 1,
				"Time and Relative Dimension in Space" );
		ValueDto ssDto = new ValueDto( 2, "Sonic Screwdriver" );
		ValueDtoMap dtoMap = new ValueDtoMap();
		dtoMap.put( "TARDIS", tardisDto );
		dtoMap.put( "SS", ssDto );
		ValueMap valueMap = new ValueMap();
		Value tardis = new Value( 1,
				"Dr. Who's Ship" );
		valueMap.put( "TARDIS", tardis );

		Update.from( dtoMap ).to( valueMap );

		assertEquals( 2, valueMap.size() );
		assertIdentityAndValues( tardis, tardisDto, valueMap.get( "TARDIS" ) );
		assertEqualValues( ssDto, valueMap.get( "SS" ) );
	}

	@Test
	public void testUpdateMapWillNullifyItemWhoseKeyHasNullValueInSourceMap() {
		ValueDto iphone4sDto = new ValueDto( 1, "HSPA+" );
		ValueDto nexusDto = new ValueDto( 3, "LTE" );

		ValueDtoMap dtoMap = new ValueDtoMap();
		dtoMap.put( "iPhone 4S", iphone4sDto );
		dtoMap.put( "Galaxy Nexus", nexusDto );
		dtoMap.put( "iPhone 4", null );

		Value iphone4s = new Value( 1, "4G" );
		Value nexus = new Value( 3, "4G" );

		ValueMap valueMap = new ValueMap();
		valueMap.put( "iPhone 4S", iphone4s );
		valueMap.put( "iPhone 4", new Value( 2, "3G" ) );
		valueMap.put( "Galaxy Nexus", nexus );

		Update.from( dtoMap ).to( valueMap );

		assertEquals( 3, valueMap.size() );
		assertIdentityAndValues( iphone4s, iphone4sDto,
				valueMap.get( "iPhone 4S" ) );
		assertIdentityAndValues( nexus, nexusDto,
				valueMap.get( "Galaxy Nexus" ) );
		assertTrue(
				"Value Map should have retained key, but set to null after update.",
				valueMap.containsKey( "iPhone 4" ) );
		assertNull( valueMap.get( "iPhone 4" ) );
	}

	@Test
	public void testUpdateMapWillRemoveItemWhoseKeyIsNotPresentInSourceMap() {
		ValueDto iphone4sDto = new ValueDto( 1, "HSPA+" );
		ValueDto nexusDto = new ValueDto( 3, "LTE" );

		ValueDtoMap dtoMap = new ValueDtoMap();
		dtoMap.put( "iPhone 4S", iphone4sDto );
		dtoMap.put( "Galaxy Nexus", nexusDto );

		Value iphone4s = new Value( 1, "4G" );
		Value nexus = new Value( 3, "4G" );

		ValueMap valueMap = new ValueMap();
		valueMap.put( "iPhone 4S", iphone4s );
		valueMap.put( "iPhone 4", new Value( 2, "3G" ) );
		valueMap.put( "Galaxy Nexus", nexus );

		Update.from( dtoMap ).to( valueMap );

		assertEquals( 2, valueMap.size() );
		assertIdentityAndValues( iphone4s, iphone4sDto,
				valueMap.get( "iPhone 4S" ) );
		assertIdentityAndValues( nexus, nexusDto,
				valueMap.get( "Galaxy Nexus" ) );
		assertFalse( valueMap.containsKey( "iPhone 4" ) );
	}

	@Test
	public void testUpdateMapWillNotRemoveItemWhoseKeyIsNotPresentInSourceMapIfRemoveOrphansIsFalse() {
		ValueDtoMap dtoMap = new ValueDtoMap();
		dtoMap.put( "iPhone 4S", new ValueDto( 1, "HSPA+" ) );
		dtoMap.put( "Galaxy Nexus", new ValueDto( 3, "LTE" ) );

		RetainValueMap valueMap = new RetainValueMap();
		valueMap.put( "iPhone 4S", new Value( 1, "4G" ) );
		valueMap.put( "iPhone 4", new Value( 2, "3G" ) );
		valueMap.put( "Galaxy Nexus", new Value( 3, "4G" ) );

		Update.from( dtoMap ).to( valueMap );

		assertEquals( 3, valueMap.size() );
	}

}