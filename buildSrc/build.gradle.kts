import groovy.util.Node
import groovy.xml.XmlParser
import groovy.xml.XmlUtil
import java.io.File

tasks.named("build") {
	doLast {
		val ideaFolder = File(".idea")
		if (ideaFolder.exists() && ideaFolder.isDirectory) {
			val xmlFile = File(".idea/vcs.xml")
			if (xmlFile.exists() && xmlFile.isFile) {
				val xml = XmlParser().parse(xmlFile)
				val issueNavigationConfiguration = xml
						.children()
						.filterIsInstance<Node>()
						.firstOrNull {
							it.name() == "component"
									&& it.attribute("name") == "IssueNavigationConfiguration"
						}
				if (issueNavigationConfiguration != null) {
					xml.remove(issueNavigationConfiguration)
				}
				val component = xml.appendNode("component", mapOf("name" to "IssueNavigationConfiguration"))
				val option = component.appendNode("option", mapOf("name" to "links"))
				val list = option.appendNode("list")
				val issueNavigationLink = list.appendNode("IssueNavigationLink")
				issueNavigationLink.appendNode("option", mapOf("name" to "issueRegexp", "value" to "#(\\d+)"))
				issueNavigationLink.appendNode(
					"option",
					mapOf("name" to "linkRegexp", "value" to "https://github.com/junit-pioneer/junit-pioneer/issues/\$1")
				)
				xmlFile.writer().use { writer ->
					XmlUtil.serialize(xml, writer)
				}
			}
		}
	}
}
