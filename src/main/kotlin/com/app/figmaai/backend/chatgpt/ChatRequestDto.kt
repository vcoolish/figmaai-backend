package com.app.figmaai.backend.chatgpt

class ChatCopyrightRequestDto(
  val text: String,
  val mode: CopyrightMode,
  val language: ChatGptLanguage?,
  val tone: ChatGptTone?,
)

class UxRequestDto(
  val text: String,
  val mode: UxMode,
)

enum class CopyrightMode(
  val system: String,
  val request: String,
  val copies: Int,
  val title: String
) {
  paraphrase(
    system = """
      1. If I ask you to paraphrase the given text, always provide me one paraphrased version. No other content should be in your answer.
      2. If I ask you to paraphrase the given text, I may also specify the desired tone of voice for the paraphrased result. The final 3 paraphrased results should be in the style of the specified tone of voice.
      3. If I ask you to paraphrase the given text, try not to excessively shorten or expand the text. For example, if I provided a text with 3 sentences, there's no need to create a paraphrased version with 6 or more sentences or with only 1 sentence. 4. If I ask you to paraphrase the given text, the final 3 paraphrased versions
      should always retain the meaning of the original text I provided with original language.
      4. If I ask you to paraphrase the given text, follow each rule for the 'Paraphrase the text' command without exception.
    """.trimIndent(),
    request = "Paraphrase the text %s: %s",
    copies = 3,
    title = "Rephrase",
  ),
  enlonger(
    system = """
      1. If I ask you to make the given text longer, always provide me one extended version of the text. No other content should be in your answer.
      2. If I ask you to make the given text longer, the final 3 extended versions should always retain the meaning of the original text I provided.
      3. If I ask you to make the given text longer, I may also specify the desired tone of voice for the extended results. The final extended results should be in the style of the specified tone of voice.
      4. If I ask you to make the given text longer, the final extended text should be no more than 2 times the size of the total word and character count of the text I have provided matching original language.
      5. If I ask you to make the given text longer, follow each rule for the 'Paraphrase the text' command without exception.
    """.trimIndent(),
    request = "Make the text longer %s: %s",
    copies = 3,
    title = "Expand",
  ),
  enshorter(
    system = """
      1. If I ask you to make the given text shorter, always provide me one shortened version of the text. No other content should be in your answer.
      2. If I ask you to make the given text shorter, the final shortened version should always retain the meaning of the original text I provided with original language.
      3. The final versions should always aim to contain fewer characters and words
      than the original text, while preserving the meaning of the original text. If it is not possible to further shorten the text without losing its meaning, it would be acceptable to maintain the same word or character count as the original text.
      4. If I ask you to make the given text shorter, I may also specify the desired tone of voice for the shortened results. The final 3 shortened results should be in the style of the specified tone of voice.
      5. If I ask you to make the given text shorter, follow each rule for the 'Make the text shorter' command without exception.
    """.trimIndent(),
    request = "Make the text shorter %s: %s",
    copies = 3,
    title = "Condense",
  ),
  fix(
    system = """
      1. If I ask you to correct errors in the given text, you should correct grammatical errors in that text. For example, if a word is misspelled, if a word is in the wrong position within the sentence, if a comma is missing, and so on. No other content should be in your answer.
      2. If I ask you to correct errors in the given text, you should not paraphrase the text by replacing words with synonyms with original language.
      3. If I ask you to correct errors in the given text, always provide me with 1 corrected version of the original text.
      4. If I ask you to correct errors in the given text, follow each rule for the 'Correct errors in the text' command.
    """.trimIndent(),
    request = "Correct errors in the text %s: %s",
    copies = 1,
    title = "Fix grammar",
  ),
  translate(
    system = """
      1. If I ask you to translate the given text into the specified language, you should provide me with 1 translated result. No other content should be in your answer.
      2. If I ask you to translate the given text into the specified language, keep in mind that I can provide the text in any language and specify which language to translate it into.
      3.If I ask you to translate the given text into the specifiled language, the final paraphrased version should always retain the meaning of the original text I provided.
      3. If I ask you to translate the given text into the specified language, follow each rule for the 'Translate the text into another language' command without exception.
    """.trimIndent(),
    request = "Translate the text into %s language %s: %s",
    copies = 1,
    title = "Translate to",
  ),
}

enum class UxMode(val value: String, val title: String, val inputs: Map<String, String>) {
  userflow(
    "",
    "User flow",
    mapOf(
      "Product description" to "A mobile app for safely buying, sending, and storing your crypto assets",
      "User persona" to "32-year-old crypto investor from London wants to find a way to safely store his assets. Flow Scenario",
      "Flow Scenario" to "Connect a wallet when using the app for the first time",
    ),
  ),
  ujm(
    """
    Your goal is to create user journey maps (UJM) based on the following template and represent the result in trimmed to one line JSON tree format keeping key names as in example: 
    {
      "stages": [
        {
          "stage": "Stage 1 title up to 20 characters",
          "actions": [
            "Action 1",
            "Action 2"
          ],
          "touchpoints": [
            "Touchpoint 1",
            "Touchpoint 2"
          ],
          "user_thoughts": [
            "User thought 1",
            "User thought 2"
          ],
          "user_feelings": [
            "User feeling 1",
            "User feeling 2"
          ],
          "pain_points": [
            "Pain point 1",
            "Pain point 2"
          ],
          "opportunities": [
            "Opportunity 1",
            "Opportunity 2"
          ]
        },
        {
          "stage": "Stage 2 title up to 20 characters",
          "actions": [
            "Action 1",
            "Action 2"
          ],
          "touchpoints": [
            "Touchpoint 1",
            "Touchpoint 2"
          ],
          "user_thoughts": [
            "User thought 1",
            "User thought 2"
          ],
          "user_feelings": [
            "User feeling 1",
            "User feeling 2"
          ],
          "pain_points": [
            "Pain point 1",
            "Pain point 2"
          ],
          "opportunities": [
            "Opportunity 1",
            "Opportunity 2"
          ]
        }
      ]
    }
      
      This template should be filled out for each stage of the user's journey, from the beginning to the end of their interaction with the service or system. The number of stages can vary depending on the complexity of the user's journey and the depth of the analysis required.».
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
    """.trimIndent(),
    "UJM",
    mapOf(
      "Product description" to "A mobile app for safely buying, sending, and storing your crypto assets",
      "User persona" to "32-year-old crypto investor from London wants to find a way to safely store his assets. Flow Scenario",
      "Scenario" to "User browsing the App Store for crypto apps",
    ),
  ),
  userpersona(
    """
    Your goal is to create user personas based on the following template and represent the result in valid trimmed to one line JSON tree format avoiding boolean values and keeping key names as in example: 
    {
      "persona_name": "Persona name up to 20 characters",
      "basic_info": {
        "age": "Age of the persona as integer number",
        "gender": "Gender of the persona in format of male or female",
        "location": "Location or place of residence of the persona",
        "occupation": "Current job or profession of the persona",
        "education": "Educational background of the persona",
        "tech_literacy": "Level of technological literacy (low, medium, high). * Economic Status: Economic or financial status of the persona"
      },
      "persona_quote": "A quote that could be attributed to the persona that represents their needs, goals, or personality",
      "needs": "An analysis of the user's needs, tasks, or goals",
      "pain_points": "Issues or problems the user persona might encounter",
      "behaviors_and_preferences": {
        "devices": "Devices (desktop, mobile, tablet, etc.)",
        "operating_systems": "Operating systems",
        "browsers": "Browsers",
        "channels": "Channels (communication and interaction preferences)",
        "social_media": "Social media",
        "websites": "Websites",
        "mobile_applications: "Mobile applications",
        "email: "Random Email example",
        "phone": "Random Phone example"
      },
      "psychographic_profile": {
        "personality_type": "Introvert/Extrovert, decision-making traits, approach to problem solving, emotional intelligence.",
        "values_and_beliefs:": "Personal values, ethical standards, environmental and social considerations"
     },
     "character_traits": [
        "Description of first character trait that is consequential to the context provided in the project description.",
        "Description of second character trait that is consequential to the context provided in the project description.",
        "Description of third character trait that is consequential to the context provided in the project description.",
        "Description of fourth character trait that is consequential to the context provided in the project description."
      ]
    }
      When creating a persona, you should always base yourself on the following rules: "
      1. The first and last names should be absolutely similar to people's real names. Try not to choose the most popular combinations.
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
     """.trimIndent(),
    "User persona",
    mapOf(
      "Product description" to "A mobile app for safely buying, sending, and storing your crypto assets",
    ),
  ),
  mindmap(
    """
      Your goal is to create Mind Maps. When creating a mind map, you should always base yourself on the following rules and represent the result in trimmed to one line JSON tree format keeping key names as in example:
      {
        "central_node": "Central Node:",
        "main_nodes": [
          {
            "title": "Main Node 1 title",
            "sub_nodes": [
              {
                "title": "Sub Node 1 title",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1 title"
                  },
                  {
                    "title": "Sub Sub Node 2 title"
                  }
                ]
              },
              {
                "title": "Sub Node 2 title",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1 title"
                  },
                  {
                    "title": "Sub Sub Node 2 title"
                  }
                ]
              }
            ]
          },
          {
            "title": "Main Node 2 title",
            "sub_nodes": [
              {
                "title": "Sub Node 1 title",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1 title"
                  },
                  {
                    "title": "Sub Sub Node 2 title"
                  }
                ]
              },
              {
                "title": "Sub Node 2 title",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1 title"
                  },
                  {
                    "title": "Sub Sub Node 2 title"
                  }
                ]
              }
            ]
          }
        ]
      }
      "Rules:
      1. Create a Mind Map based on the information provided, viz:
      - Goals/Objectives: The main goals or objectives of the project or business. - Success Metrics: Key Performance Indicators (KPIs) that will be used to
      measure the success of the project or business.
      - Stakeholders: People or groups who are interested or involved in the project or
      business.
      2. Identify the central node: This node is the main theme or idea of the mind map. It will be defined based on the information provided (goals/objectives, success metrics, stakeholders). The central node should always fit into a concise sentence, ideally 3-4 words. Label it "Central Node:".
      3. Main branches diverging from the central node: The main branches diverging from the central node will be determined based on the specific context of the information provided. Main nodes should also fit into 1-3 words if possible. Label them as "Main Node 1:", "Main Node 2:", etc.
      4. Add sub-nodes: These sub-nodes represent the details of the main branches. The number and depth of the sub-nodes depends on the complexity of the information provided. There is no limit to the number of sub-nodes. Label them as "Node 1.1:", "Node 1.2:", etc.
      5. Add sub-sub-nodes: These sub-sub-nodes represent the details of the sub-nodes. The number and depth of the sub-branches depends on the complexity of the information provided. There is no limit to the number of sub-branches. Label them as "Sub-branch 1.1:", "Sub-branch 1.2:", etc.
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
    """.trimIndent(),
    "Mind map",
    mapOf(
      "Goals/Objectives" to "Create AI support chat-bot as a SaaS application for businesses",
      "Metrics for Success" to "5000 active users by the end of the year",
      "Stakeholders" to "Business Owners, Clients of businesses",
    ),
  ),
  sitemap(
    "",
    "Sitemap",
    mapOf(
      "Main sections and pages of the site" to "Home page, Login page, User office, Privacy policy, Terms of use, Blog.",
      "Navigation structure" to "Home page-Login page-User office",
      "User flow" to "User logs in to his account and goes to his personal cabinet to manage the subscription",
    ),
  )
}