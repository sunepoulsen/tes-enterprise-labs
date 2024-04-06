package dk.sunepoulsen.tel.testdata.module.ct

import dk.sunepoulsen.tel.testdata.module.integrator.TelTestDataIntegrator
import dk.sunepoulsen.tes.docker.containers.ClasspathPropertiesDockerImageProvider
import dk.sunepoulsen.tes.docker.containers.DockerImageProvider
import dk.sunepoulsen.tes.docker.containers.TESBackendContainer
import dk.sunepoulsen.tes.docker.containers.TESContainerSecureProtocol
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.config.DefaultClientConfig
import dk.sunepoulsen.tes.rest.integrations.config.TechEasySolutionsClientConfig
import dk.sunepoulsen.tes.security.net.ssl.SSLContextFactory
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import org.testcontainers.containers.Network
import org.testcontainers.utility.MountableFile

import javax.net.ssl.SSLContext

@Slf4j
class DeploymentSpockExtension implements IGlobalExtension {
    private static TESBackendContainer telTestDataBackendContainer = null
    private DockerImageProvider dockerImageProvider = new ClasspathPropertiesDockerImageProvider('/deployment.properties', 'tel-testdata')
    private static Properties deploymentProperties = loadDeploymentProperties()

    static Properties loadDeploymentProperties() {
        Properties props = new Properties()
        props.load(DeploymentSpockExtension.class.getResourceAsStream('/deployment.properties') )
        return props
    }

    static TESBackendContainer telTestDataBackendContainer() {
        return telTestDataBackendContainer
    }

    static TelTestDataIntegrator telTestDataBackendIntegrator() {
        SSLContext sslContext = SSLContextFactory.createSSLContext(new File(deploymentProperties.getProperty('ssl.key-store')), deploymentProperties.getProperty('ssl.key-store-password'))

        TechEasySolutionsClientConfig clientConfig = new DefaultClientConfig(sslContext)
        TechEasySolutionsClient client = telTestDataBackendContainer.createClient(clientConfig)

        return new TelTestDataIntegrator(client)
    }

    @Override
    void start() {
        Network network = Network.newNetwork()

        log.debug("Current directory: {}", new File(".").absolutePath)
        telTestDataBackendContainer = new TESBackendContainer(dockerImageProvider, new TESContainerSecureProtocol(), 'ct')
            .withConfigMapping('application-ct.properties')
            .withCopyFileToContainer(MountableFile.forHostPath(deploymentProperties.getProperty('ssl.key-store')), "/app/certificates/${deploymentProperties.getProperty('ssl.key-store.filename')}")
            .withNetwork(network)
        telTestDataBackendContainer.start()

        log.info('TEL TestData Module Exported Port: {}', telTestDataBackendContainer.getMappedPort(8080))
    }

    @Override
    void visitSpec(SpecInfo spec) {
    }

    @Override
    void stop() {
        telTestDataBackendContainer.copyLogFile('build/logs/tel-testdata-module.log')
        telTestDataBackendContainer.stop()
    }

}
