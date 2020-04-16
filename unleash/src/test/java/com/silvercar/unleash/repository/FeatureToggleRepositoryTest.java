package com.silvercar.unleash.repository;

import com.silvercar.unleash.ActivationStrategy;
import com.silvercar.unleash.FeatureToggle;
import com.silvercar.unleash.util.UnleashConfig;
import com.silvercar.unleash.util.UnleashConfigBuilder;
import com.silvercar.unleash.util.UnleashScheduledExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


public class FeatureToggleRepositoryTest {

    @Test
    public void no_backup_file_and_no_repository_available_should_give_empty_repo() {
        UnleashConfig config = new UnleashConfigBuilder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                .build();
        ToggleFetcher toggleFetcher = new HttpToggleFetcher(config);
        ToggleBackupHandler toggleBackupHandler = new ToggleBackupHandlerFile(config);
        ToggleRepository toggleRepository = new FeatureToggleRepository(config, toggleFetcher, toggleBackupHandler);
        assertNull(toggleRepository.getToggle("unknownFeature"), "should be null");
    }

    @Test
    public void backup_toggles_should_be_loaded_at_startup() {
        UnleashConfig config = new UnleashConfigBuilder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .fetchTogglesInterval(Long.MAX_VALUE)
                .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                .build();

        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        new FeatureToggleRepository(config, toggleFetcher, toggleBackupHandler);

        verify(toggleBackupHandler, times(1)).read();
    }

    @Test
    public void feature_toggles_should_be_updated() throws URISyntaxException, InterruptedException {
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);

        //setup backupHandler
        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);
        ToggleCollection toggleCollection = populatedToggleCollection(
                new FeatureToggle("toggleFetcherCalled", false, Arrays.asList(new ActivationStrategy("custom", null))));
        when(toggleBackupHandler.read()).thenReturn(toggleCollection);

        //setup fetcher
        toggleCollection = populatedToggleCollection(
                new FeatureToggle("toggleFetcherCalled", true, Arrays.asList(new ActivationStrategy("custom", null))));
        FeatureToggleResponse response = new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, 200, toggleCollection, null);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        //init
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);


        UnleashConfig config = new UnleashConfigBuilder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .fetchTogglesInterval(200l)
                .scheduledExecutor(executor)
                .build();

        ToggleRepository toggleRepository = new FeatureToggleRepository(config, toggleFetcher, toggleBackupHandler);

        //run the toggleName fetcher callback
        verify(executor).setInterval(runnableArgumentCaptor.capture(), anyLong(), anyLong());
        verify(toggleFetcher, times(0)).fetchToggles();
        runnableArgumentCaptor.getValue().run();

        verify(toggleBackupHandler, times(1)).read();
        verify(toggleFetcher, times(1)).fetchToggles();
        assertTrue(toggleRepository.getToggle("toggleFetcherCalled").isEnabled());
    }

    @Test
    public void get_feature_names_should_return_list_of_names() {
        UnleashConfig config = new UnleashConfigBuilder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .environment("test")
                .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                .build();
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);

        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);
        ToggleCollection toggleCollection = populatedToggleCollection(
                new FeatureToggle("toggleFeatureName1", true, Arrays.asList(new ActivationStrategy("custom", null))),
                new FeatureToggle("toggleFeatureName2", true, Arrays.asList(new ActivationStrategy("custom", null)))
        );
        when(toggleBackupHandler.read()).thenReturn(toggleCollection);

        ToggleRepository toggleRepository = new FeatureToggleRepository(config, toggleFetcher, toggleBackupHandler);
        assertTrue(2 == toggleRepository.getFeatureNames().size());
        assertTrue("toggleFeatureName2".equals(toggleRepository.getFeatureNames().get(1)));
    }

    @Test
    public void should_perform_synchronous_fetch_on_initialisation() {
        UnleashConfig config = new UnleashConfigBuilder()
                .synchronousFetchOnInitialisation(true)
                .appName("test-sync-update")
                .unleashAPI("http://localhost:8080")
                .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                .build();
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);

        //setup fetcher
        ToggleCollection toggleCollection = populatedToggleCollection();
        FeatureToggleResponse response = new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, 200, toggleCollection, null);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        new FeatureToggleRepository(config, toggleFetcher, toggleBackupHandler);

        verify(toggleFetcher, times(1)).fetchToggles();
    }

    @Test
    public void should_not_perform_synchronous_fetch_on_initialisation() {
        UnleashConfig config = new UnleashConfigBuilder()
                .synchronousFetchOnInitialisation(false)
                .appName("test-sync-update")
                .unleashAPI("http://localhost:8080")
                .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                .build();
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);

        //setup fetcher
        ToggleCollection toggleCollection = populatedToggleCollection();
        FeatureToggleResponse response = new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, 200, toggleCollection, null);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        new FeatureToggleRepository(config, toggleFetcher, toggleBackupHandler);

        verify(toggleFetcher, times(0)).fetchToggles();
    }

    private ToggleCollection populatedToggleCollection(FeatureToggle... featureToggles) {
        List<FeatureToggle> list = new ArrayList();
        list.addAll(Arrays.asList(featureToggles));
        return new ToggleCollection(list);

    }

}
