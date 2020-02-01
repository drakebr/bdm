import BdmDockerKeys._

enablePlugins(BdmDockerPlugin, ItTestPlugin)

description := "NODE integration tests"
libraryDependencies ++= Dependencies.it

def stageFiles(ref: ProjectReference): TaskKey[File] =
  ref / Universal / stage

inTask(docker)(
  Seq(
    imageNames := Seq(ImageName("com.bdmplatform/node-it")),
    exposedPorts := Set(6863, 6869, 6870), // NetworkApi, RestApi, gRPC
    additionalFiles ++= Seq(
      stageFiles(LocalProject("node")).value,
      stageFiles(LocalProject("grpc-server")).value,
      (Test / resourceDirectory).value / "template.conf",
      (Test / sourceDirectory).value / "container" / "start-bdm.sh"
    )
  )
)
