package com.codiform.moo;

import com.codiform.moo.annotation.Property;
import com.codiform.moo.domain.TestFactory;
import com.codiform.moo.session.TestableTranslationSession;
import com.codiform.moo.session.TranslationSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * This isn't a test of the TranslationTargetFactory, which is an interface, but rather of the
 * infrastructure that uses translation target factories during translation, to ensure that a
 * factory can cause the desired outcome. The factories themselves will be mocked.
 */
@RunWith( MockitoJUnitRunner.class )
@SuppressWarnings( "unused" )
public class TranslationTargetFactoryTest {

	@Mock
	private TestFactory factory;

	private TranslationSession session;

	@Before
	public void setUp() {
		TestableTranslationSession testSession = new TestableTranslationSession();
		testSession.cacheTranslationTargetFactory( TestFactory.class, factory );
		session = testSession;
	}

	@Test
	public void testInvokesFactoryForInstantiation() {
		PetOwner domain = new PetOwner( new Animal( "Perry the Platypus" ) );
		Mockito.when( factory.getTranslationTargetInstance( domain.getPet(), AnimalDto.class ) ).thenReturn( new AnimalDto() );

		PetOwnerDto dto = session.getTranslation( domain, PetOwnerDto.class );

		assertNotNull( dto );
		assertNotNull( dto.getPet() );
		assertEquals( AnimalDto.class, dto.getPet().getClass() );
		assertEquals( "Perry the Platypus", dto.getPet().getName() );
	}

	@Test
	public void testFactoryCanSubstituteSubclass() {
		PetOwner domain = new PetOwner( new Cat( "Chester Cheetah" ) );
		Mockito.when( factory.getTranslationTargetInstance( any( Cat.class ), eq( AnimalDto.class ) ) ).thenReturn( new CatDto() );

		PetOwnerDto dto = session.getTranslation( domain, PetOwnerDto.class );

		assertNotNull( dto );
		assertNotNull( dto.getPet() );
		assertEquals( CatDto.class, dto.getPet().getClass() );
		assertEquals( "Chester Cheetah", dto.getPet().getName() );
	}

	@Test
	public void testFactorySetButTranslateIsFalse() {
		PetOwner source = new PetOwner( new Animal( "Echo echo" ) );

		PetOwnerCopy destination = session.getTranslation( source, PetOwnerCopy.class );

		assertNotNull( destination );
		assertNotNull( destination.getPet() );
		assertEquals( source.getPet(), destination.getPet() );
		assertSame( source.getPet(), destination.getPet() );
	}

	private static class PetOwner {
		private Animal pet;

		public PetOwner( Animal pet ) {
			this.pet = pet;
		}

		public Animal getPet() {
			return pet;
		}
	}

	private static class PetOwnerDto {
		@Property( translate = true, factory = TestFactory.class )
		private AnimalDto pet;

		public AnimalDto getPet() {
			return pet;
		}
	}

	private static class PetOwnerCopy {
		@Property( translate = false, factory = TestFactory.class )
		private Animal pet;

		public Animal getPet() {
			return pet;
		}
	}

	private static class Animal {
		private String name;

		public Animal( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private static class Cat extends Animal {
		public Cat( String name ) {
			super( name );
		}
	}

	private static class Dog extends Animal {
		public Dog( String name ) {
			super( name );
		}
	}

	private static class AnimalDto {
		private String name;

		public String getName() {
			return name;
		}
	}

	private static class CatDto extends AnimalDto {
	}

	private static class DogDto extends AnimalDto {
	}
}
