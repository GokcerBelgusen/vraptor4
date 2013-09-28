package br.com.caelum.vraptor.serialization;

import static java.util.Collections.sort;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.http.FormatResolver;
import br.com.caelum.vraptor.other.pack4ge.DumbSerialization;
import br.com.caelum.vraptor.serialization.xstream.XStreamJSONSerialization;
import br.com.caelum.vraptor.serialization.xstream.XStreamXMLSerialization;
import br.com.caelum.vraptor.view.PageResult;
import br.com.caelum.vraptor.view.Status;

public class DefaultRepresentationResultTest {

	private @Mock FormatResolver formatResolver;
	private @Mock Serialization serialization;
	private @Mock Result result;
	private @Mock PageResult pageResult;
	private @Mock Status status;

	private RepresentationResult representation;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(result.use(PageResult.class)).thenReturn(pageResult);
		when(result.use(Status.class)).thenReturn(status);
		representation = new DefaultRepresentationResult(formatResolver, result, Arrays.asList(serialization));
	}

	@Test
	public void whenThereIsNoFormatGivenShouldForwardToDefaultPage() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn(null);

		Serializer serializer = representation.from(new Object());

		assertThat(serializer, is(instanceOf(IgnoringSerializer.class)));

		verify(status).notAcceptable();
	}


	@Test
	public void shouldSend404IfNothingIsRendered() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn(null);

		Serializer serializer = representation.from(null);

		assertThat(serializer, is(instanceOf(IgnoringSerializer.class)));

		verify(status).notFound();
	}

	@Test
	public void whenThereIsNoFormatGivenShouldForwardToDefaultPageWithAlias() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn(null);

		Object object = new Object();
		Serializer serializer = representation.from(object, "Alias!");

		assertThat(serializer, is(instanceOf(IgnoringSerializer.class)));

		verify(status).notAcceptable();
	}
	@Test
	public void whenThereIsAFormatGivenShouldUseCorrectSerializer() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn("xml");

		when(serialization.accepts("xml")).thenReturn(true);
		Object object = new Object();

		representation.from(object);

		verify(serialization).from(object);
	}
	@Test
	public void whenThereIsAFormatGivenShouldUseCorrectSerializerWithAlias() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn("xml");

		when(serialization.accepts("xml")).thenReturn(true);
		Object object = new Object();

		representation.from(object, "Alias!");

		verify(serialization).from(object, "Alias!");
	}
	@Test
	public void whenSerializationDontAcceptsFormatItShouldntBeUsed() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn("xml");

		when(serialization.accepts("xml")).thenReturn(false);
		Object object = new Object();

		representation.from(object);

		verify(serialization, never()).from(object);
	}

	@Test
	public void shouldSortBasedOnPackageNamesLessPriorityToCaelumInitialList3rdPartyFirst() {
		List<Serialization> serializers = new ArrayList<>();

		serializers.add(new DumbSerialization());
		serializers.add(new XStreamXMLSerialization(null, null));
		serializers.add(new XStreamJSONSerialization(null, null, null));
		serializers.add(new HTMLSerialization(null, null));

		sort(serializers, new DefaultRepresentationResult.ApplicationPackageFirst());

		assertEquals("br.com.caelum.vraptor.other.pack4ge", serializers.get(0).getClass().getPackage().getName());

	}

	@Test
	public void shouldSortBasedOnPackageNamesLessPriorityToCaelumInitialList3rdPartyLast() {
		List<Serialization> serializers = new ArrayList<>();

		serializers.add(new XStreamXMLSerialization(null, null));
		serializers.add(new XStreamJSONSerialization(null, null, null));
		serializers.add(new HTMLSerialization(null, null));
		serializers.add(new DumbSerialization());

		sort(serializers, new DefaultRepresentationResult.ApplicationPackageFirst());

		assertEquals("br.com.caelum.vraptor.other.pack4ge", serializers.get(0).getClass().getPackage().getName());
	}
}
