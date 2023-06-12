package com.app.figmaai.backend.chatgpt

class ChatCopyrightRequestDto(
  val text: String,
  val mode: CopyrightMode,
  val language: String? = "english",
)

class UxRequestDto(
  val text: String,
  val mode: UxMode,
)

enum class CopyrightMode(val request: String) {
  paraphrase("Paraphrase the following text.\n"),
  enlonger("Make the following text longer.\n"),
  enshorter("Make the following text shorter.\n"),
  translate("Translate the following text into %s language.\n"),
  fix("Correct grammatical errors in the following text without paraphrasing.\n"),
}

enum class UxMode(val value: String) {
  map(
    """
      Your goal is to create Mind Maps. When creating a mind map, you should always base yourself on the following rules:
      "Rules:
      1. Create a Mind Map based on the information provided, viz:
      - Goals/Objectives: The main goals or objectives of the project or business. - Success Metrics: Key Performance Indicators (KPIs) that will be used to
      measure the success of the project or business.
      - Stakeholders: People or groups who are interested or involved in the project or
      business.
      2. Identify the central node: This node is the main theme or idea of the mind map. It will be defined based on the information provided (goals/objectives, success metrics, stakeholders). The central node should always fit into a concise sentence, ideally 3-4 words. Label it "Central Node:".
      3. Main branches diverging from the central node: The main branches diverging from the central node will be determined based on the specific context of the information provided. Main nodes should also fit into 1-3 words if possible. Label them as "Main Node 1:", "Main Node 2:", etc.
      4. Add sub-nodes: These sub-nodes represent the details of the main branches. The number and depth of the sub-nodes depends on the complexity of the information provided. There is no limit to the number of sub-nodes. Label them as "Node 1.1:", "Node 1.2:", etc.
      5. Add sub-nodes: These sub-nodes represent the details of the sub-nodes. The number and depth of the sub-branches depends on the complexity of the information provided. There is no limit to the number of sub-branches. Label them as "Sub-branch 1.1:", "Sub-branch 1.2:", etc.
      6. Describe each sub-industry in detail: Each sub-industry should be described in detail based on the information provided and how it relates to the main industry from which it originated.
      7. Visual representation: The map should visually represent the hierarchy and connections between the central node, major branches and sub-branches.
      8. Whenever I tell you to create a mental map, always follow all the rules without exception.
      9. If I don't give any information on goals/objectives, indicators of success, or stakeholders, don't create a map.
      10. Every time I tell you to make a mental map, you must follow the latest updated version of the rules. Remember to follow every rule item.
      11. Every time you are told to create a mind map, create it the way a senior UX designer with 10 years of experience would.
      12. If some of the information from the Goals/Objectives, Success Metrics, Stakeholders sections is not provided, still try to create a mind map, but remember to do it within the given limits.
      13. Since the structure of the map can vary depending on the information provided, always create the map according to the following scheme: 1 central
      node, 4 main nodes, 3 sub-nodes for each main node, 3 sub-branches for each sub-node.
      14. Use the information provided in the Goals/Actions, Metrics for Success, Stakeholders sections as a context for analysis to determine the structure of the mind map. Do not include this information directly into the mind map.
      15. The central node in the mind map must fit into 3-4 words at most." . Always follow each point of these rules without exception.
    """.trimIndent()
  ),
  ujm(
    """
    Your goal is to create user journey maps (UJM) based on the following template: « Stage Name:
      1. Actions: (What actions does the user take at this stage?)
      2. Touchpoints: (What parts of the system or service does the user interact
      with?)
      3. User Thoughts: (What might the user be thinking at this stage?)
      4. User Feelings: (What might the user be feeling at this stage?)
      5. Pain Points: (What issues or obstacles does the user encounter at this stage?) 6. Opportunities: (Are there any opportunities for improvement at this stage?) This template should be filled out for each stage of the user's journey, from the beginning to the end of their interaction with the service or system. The number of stages can vary depending on the complexity of the user's journey and the depth of the analysis required.».
      When creating a UJM, you should always base yourself on the following rules: "Always create a UJM based on the last updated version of the template.
      1. Create a UJM based on information provided which always will be User Persona, Scenario/Context, and Project Description.
      2. Every time you are told to create a UJM but the rules aren't mentioned - always follow the rules anyway.
      3. Every time you are told to create a UJM but the template isn't mentioned, always create the UJM strictly from the template anyway.
      4. If any of the necessary information (User Persona, Scenario/Context, Project Description) isn't provided, ask for it.
      5. Every time you create a UJM, follow every rule without exception.
      6. Always create as many stages for UJM as needed to show the user journey
      as accurately as possible, not limiting to any fixed number of stages.
      7. Even if information provided about user persona, project description, and
      scenario is not complete enough, use the provided information to the
      maximum to achieve the best result in UJM creation.
      8. Every time you are told to create a UJM, create it as a Senior UX designer
      with 10 years of experience would.
      9. Always create a UJM based on the last updated version of the rules." .
      Always follow each point of these rules without exception.
    """.trimIndent()
  ),
  persona(
    """
    Your goal is to create user personas based on the following template: «**1. Persona Name**
      * The name of the persona.
       **2. Basic Information**
      * Age: Age of the persona.
      * Gender: Gender of the persona.
      * Location: Location or place of residence of the persona.
      * Occupation: Current job or profession of the persona.
      * Education: Educational background of the persona.
      * Tech Literacy: Level of technological literacy (low, medium, high). * Economic Status: Economic or financial status of the persona.
      **3. User Persona Quote**
      * A quote that could be attributed to the persona that represents their needs, goals, or personality.
      **4. Needs**
      * An analysis of the user's needs, tasks, or goals.
      **5. Pain Points**
      * Issues or problems the user persona might encounter.
      **6. Behaviors and Preferences**
      * Devices (desktop, mobile, tablet, etc.)
      * Operating systems
      * Browsers
      * Channels (communication and interaction preferences)
      * Social media
      * Websites
      * Mobile applications * Email
      * Phone
      **7. Psychographic Profile**
      * Personality type (Introvert/Extrovert, decision-making traits, approach to problem solving, emotional intelligence).
      * Values and beliefs (personal values, ethical standards, environmental and social considerations).
      **8. Character Traits**
      * Description of four character traits that are consequential to the context provided in the project description.».
      When creating a persona, you should always base yourself on the following rules: "1. The first and last names should be absolutely similar to people's real names. Try not to choose the most popular combinations.
      2. For section 8, Character Traits, list four traits that are consequential to the context provided in the project description. These traits should be short phrases or words.
      
      3. The number of pain points should not be limited to a certain number. Depending on the context, always create as many pain points as necessary for a particular user persona.
      4. The person's quote for section 3, User Persona Quote, should be enclosed in quotation marks and should reflect what the person might have said during the interview. Try to create as believable a quote as possible each time.
      5. Every time I tell you to create a user persona, create it as a UX designer with 10 years of experience and lots of experience interviewing users.
      6. Always create a user persona based on the information I provide under "Project Description".
      7. If the information I provide about the project description is not complete enough to create the perfect user persona, make full use of the information I provide for the best results.
      8. If I haven't provided the project description to create a user persona, but I told you to create a user persona, ask me for the project description information.
      9. Whenever I tell you to create a user persona, always create it based on the latest version of the template.
      10. Every time I tell you to create a user persona, always create it based on the latest version of the user persona creation rules.
      11. Whenever I tell you to create a user persona, make sure that you are going to follow all the rules without exception." .
      Always follow each point of these rules without exception.
     """.trimIndent()
  )
}