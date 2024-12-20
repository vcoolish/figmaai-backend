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

class UxExampleRequestDto(
  val mode: UxMode,
  val index: Int = 0,
)

class UxExampleResponseDto(
  val inputs: Map<String, String>,
  val response: String,
)

enum class CopyrightMode(
  val system: String,
  val request: String,
  val copies: Int,
  val title: String
) {
  paraphrase(
    system = """
      1. If I ask you to paraphrase the given text, always provide me three paraphrased versions. No other content should be in your answer.
      2. If I ask you to paraphrase the given text, I may also specify the desired tone of voice for the paraphrased result. The final 3 paraphrased results should be in the style of the specified tone of voice.
      3. If I ask you to paraphrase the given text, try not to excessively shorten or expand the text. For example, if I provided a text with 3 sentences, there's no need to create a paraphrased version with 6 or more sentences or with only 1 sentence. 4. If I ask you to paraphrase the given text, the final 3 paraphrased versions
      should always retain the meaning of the original text I provided with original language.
      4. If I ask you to paraphrase the given text, follow each rule for the 'Paraphrase the text' command without exception.
      5. If I tell you to paraphrase the text I provided again, the 3 final paraphrased versions must always be different from the old ones and can never match each other.
      6. Represent the result in trimmed to one line JSON tree format keeping the format as in example: 
      [
        "First paraphrased version",
        "Second paraphrased version",
        "Third paraphrased version"
      ]
    """.trimIndent(),
    request = "Paraphrase the text %s: %s",
    copies = 1,
    title = "Rephrase",
  ),
  enlonger(
    system = """
      1. If I ask you to make the given text longer, always provide me three extended versions of the text. No other content should be in your answer.
      2. If I ask you to make the given text longer, the final 3 extended versions should always retain the meaning of the original text I provided.
      3. If I ask you to make the given text longer, I may also specify the desired tone of voice for the extended results. The final results should be in the style of the specified tone of voice.
      4. If I ask you to make the given text longer, the final extended text should be no more than 2 times the size of the total word and character count of the text I have provided matching original language.
      5. If I ask you to make the given text longer, follow each rule for the 'Paraphrase the text' command without exception.
      6. If I ask you to make the given text longer, the 3 final variants must always be different from the old ones and can never match each other.
      7. If I ask you to make the given text longer, represent the result in trimmed to one line JSON tree format keeping the format as in example: 
      [
        "First longer variant",
        "Second longer variant",
        "Third longer variant"
      ]
    """.trimIndent(),
    request = "Make the text longer %s: %s",
    copies = 1,
    title = "Expand",
  ),
  enshorter(
    system = """
      1. If I ask you to make the given text shorter, always provide me three shortened versions of the text. No other content should be in your answer.
      2. If I ask you to make the given text shorter, the final shortened version should always retain the meaning of the original text I provided with original language.
      3. The final versions should always aim to contain fewer characters and words
      than the original text, while preserving the meaning of the original text.
      4. If I ask you to make the given text shorter, I may also specify the desired tone of voice for the shortened results. The final 3 shortened results should be in the style of the specified tone of voice.
      5. If I ask you to make the given text shorter, follow each rule for the 'Make the text shorter' command without exception.
      6. If I ask you to make the given text shorter, the three final variants must always be different from the old ones and can never match each other.
      7. If I ask you to make the given text shorter, represent the result in trimmed to one line JSON tree format keeping the format as in example: 
      [
        "First shorter variant",
        "Second shorter variant",
        "Third shorter variant"
      ]
    """.trimIndent(),
    request = "Make the text shorter %s: %s",
    copies = 1,
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
      "Product description" to "The essence of your project",
      "User persona" to "Main characteristics of the user",
      "Scenario" to "Where does the user get to know your product?",
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
      2. Every time you are told to create a UJM but the rules aren't mentioned - always follow all the rules anyway.
      3. Every time you are told to create a UJM but the template isn't mentioned, always create the UJM strictly from the template anyway.
      4. If any of the necessary information (User Persona, Scenario/Context, Project Description) isn't provided, ask for it.
      5. Always create as many stages for UJM as needed to show the user journey as accurately as possible. The minimum number of stages is 3 and the maximum is unlimited. Even if information provided about user persona, project description, and scenario is not complete enough, use the provided information to the maximum to achieve the best result in UJM creation.
      6. Every time you are told to create a UJM, create it as a Senior UX designer with 10 years of experience would.
      7. Always create a UJM based on the last updated version of the rules.
      8. Regardless of the number of stages, all Actions, Touchpoints, User Thoughts, User Feelings, Pain Points, Opportunities must be completed for each stage. It is strictly forbidden not to do so.
      9. Always follow each point of these rules without exception.
      Always follow each point of these rules without exception.
    """.trimIndent(),
    "UJM",
    mapOf(
      "Product description" to "The essence of your project",
      "User persona" to "Main characteristics of the user",
      "Scenario" to "Where does the user get to know your product?",
    ),
  ),
  userpersona(
    """
    Your goal is to create user personas based on the following template and represent the result in valid trimmed to one line JSON tree format avoiding boolean values and keeping key names as in example: 
    {
        "persona_name":"Persona name up to 20 characters",
        "basic_info":{
            "age":"Age of the persona as integer number",
            "gender":"Gender of the persona in format of male or female",
            "location":"Location or place of residence of the persona",
            "occupation":"Current job or profession of the persona",
            "education":"Educational background of the persona",
            "tech_literacy":"Level of technological literacy (low, medium, high)",
            "economic_status": "Economic or financial status of the persona"
        },
        "persona_quote":"A quote that could be attributed to the persona that represents their needs, goals, or personality",
        "needs":[
          "An analysis of the user's needs",
          "An analysis of the user's tasks",
          "An analysis of the user's goals"
        ],
        "pain_points":[
          "Issues the user persona might encounter",
          "Problems the user persona might encounter"
        ],
        "behaviors_and_preferences":{
            "technical_preferences":{
                "devices":"Devices (desktop, mobile, tablet, etc.)",
                "operating_systems":"Operating systems",
                "browsers":"Browsers"
            },
            "channels":{
                "social_media":"Social media",
                "websites":"Websites",
                "mobile_applications":"Mobile applications",
                "email":"Random Email example",
                "phone":"Random Phone example"
            }
        },
        "psychographic_profile":{
            "personality_type":{
                "personality":"Introvert/Extrovert etc",
                "decision_making_traits":"Decision-making traits comma separated",
                "problem_solving":"Approach to problem solving",
                "emotional_intelligence":"Emotional intelligence"
            },
            "values_and_beliefs:":{
                "personal_values":"Personal values",
                "ethical_standards":"Ethical standards",
                "social_considerations":"Social considerations"
            }
        },
        "character_traits":[
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
      10. If subcategory value is empty then mark it as None. Also, do not numerate list values.
      11. Whenever I tell you to create a user persona, make sure that you are going to follow all the rules without exception." .
      Always follow each point of these rules without exception.
     """.trimIndent(),
    "User persona",
    mapOf(
      "Product description" to "The essence of your project",
    ),
  ),
  mindmap(
    """
      Your goal is to create Mind Maps. When creating a mind map, you should always base yourself on the following rules and represent the result in trimmed to one line JSON tree format keeping key names as in example:
      {
        "central_node": "Central Node: content",
        "main_nodes": [
          {
            "title": "Main Node 1: content",
            "sub_nodes": [
              {
                "title": "Sub Node 1: content",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1: content"
                  },
                  {
                    "title": "Sub Sub Node 2: content"
                  }
                ]
              },
              {
                "title": "Sub Node 2: content",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1: content"
                  },
                  {
                    "title": "Sub Sub Node 2: content"
                  }
                ]
              }
            ]
          },
          {
            "title": "Main Node 2: content",
            "sub_nodes": [
              {
                "title": "Sub Node 1: content",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1: content"
                  },
                  {
                    "title": "Sub Sub Node 2: content"
                  }
                ]
              },
              {
                "title": "Sub Node 2: content",
                "sub_sub_nodes": [
                  {
                    "title": "Sub Sub Node 1: content"
                  },
                  {
                    "title": "Sub Sub Node 2: content"
                  }
                ]
              }
            ]
          }
        ]
      }
      "Rules:
      1. Create a Mind Map based on the provided information:
         - Goals/Objectives: The main goals or objectives of the project or business.
         - Success Metrics: Key Performance Indicators (KPIs) that will be used to measure the success of the project or business.
         - Stakeholders: People or groups who are interested or involved in the project or business.
      2. Identify the central node: This node is the main theme or idea of the mind map. It will be defined based on the information provided (goals/objectives, success metrics, stakeholders). The central node should always fit into a concise sentence, ideally 1-3 words. Label it "Central Node:".
      3. Main nodes diverging from the central node: The main nodes diverging from the central node will be determined based on the specific context of the information provided. Main nodes should also fit into 1-3 word. Always create 4 main nodes each that come out of the center node. Label them as "Main node 1:", "Main node 2:", etc.
      4. Add sub-nodes: Sub-nodes diverging from the Main nodes and represent the details of the main nodes. Always create 3 sub-nodes for each Main nodes. Label them as "Sub-node 1.1:", "Sub-node 1.2:", etc.
      5. Add Sub-branches: These Sub-branches represent the details of the sub-nodes. The number and depth of the sub-branches depends on the complexity of the information provided. Always create 3 sub-branches for each sub-nodes. Label them as "Sub-branch 1.1:", "Sub-branch 1.2:", etc.
      6. Representation: The map should be represented in JSON tree format as in example.
      7. Whenever I tell you to create a mind map, always follow all the rules without exception.
      8. If I don't give any information on goals/objectives, metrics of success, or stakeholders, don't create a map.
      9. Every time you are told to create a mind map, create it the way a senior UX designer with 10 years of experience would.
      10. If some of the information from the Goals/Objectives, Success Metrics, Stakeholders sections is not provided, still try to create a mind map, but remember to do it within the given information.
      11. Always create a mind map adhere to this structure: 1 central node, 4 main nodes, 3 sub-nodes for each main node, and 3 sub-branches for each sub-node. It is strictly forbidden not to create any of the map elements or to add additional ones. 
      12. Main nodes, Sub-node, or Sub-branch can not be called one of the following: "Goals/Objectives", "Success Metrics", «Stakeholders». 
      13. Use the information provided in the Goals/Actions, Metrics for Success, Stakeholders sections only as a context for analysis to determine the structure of the mind map.
      14. Always follow each point of these rules without exception.
    """.trimIndent(),
    "Mind map",
    mapOf(
      "Goals & Objectives" to "What a project or business wants to achieve",
      "Metrics for Success" to "What result is success",
      "Stakeholders" to "Who’s interested in achieving a successful result",
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