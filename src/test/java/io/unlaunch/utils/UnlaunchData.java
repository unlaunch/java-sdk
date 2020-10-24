package io.unlaunch.utils;

public class UnlaunchData {

    public static final String flagsResponseFromServerWithOneFlag() {
        return  "{\n" +
                "  \"data\": {\n" +
                "    \"projectName\": \"Project 1\",\n" +
                "    \"envName\": \"Test\",\n" +
                "    \"flags\": [\n" +
                "      {\n" +
                "        \"creator\": \"MrWick\",\n" +
                "        \"lastModifiedAt\": 1585990567000,\n" +
                "        \"targetUsers\": null,\n" +
                "        \"modifier\": null,\n" +
                "        \"description\": \"\",\n" +
                "        \"rules\": [\n" +
                "          {\n" +
                "            \"isDefault\": false,\n" +
                "            \"splits\": [\n" +
                "              {\n" +
                "                \"variationId\": 9,\n" +
                "                \"id\": 16,\n" +
                "                \"rolloutPercentage\": 100,\n" +
                "                \"delete\": false\n" +
                "              }\n" +
                "            ],\n" +
                "            \"id\": 11,\n" +
                "            \"conditions\": [\n" +
                "              {\n" +
                "                \"attributeId\": 8,\n" +
                "                \"op\": \"EQ\",\n" +
                "                \"id\": 4,\n" +
                "                \"attribute\": \"account_type\",\n" +
                "                \"type\": \"string\",\n" +
                "                \"value\": \"postpaid\",\n" +
                "                \"delete\": false\n" +
                "              }\n" +
                "            ],\n" +
                "            \"priority\": 2,\n" +
                "            \"delete\": false\n" +
                "          },\n" +
                "          {\n" +
                "            \"isDefault\": true,\n" +
                "            \"splits\": [\n" +
                "              {\n" +
                "                \"variationId\": 10,\n" +
                "                \"id\": 31,\n" +
                "                \"rolloutPercentage\": 100,\n" +
                "                \"delete\": false\n" +
                "              }\n" +
                "            ],\n" +
                "            \"id\": 9,\n" +
                "            \"conditions\": [],\n" +
                "            \"priority\": 0,\n" +
                "            \"delete\": false\n" +
                "          },\n" +
                "          {\n" +
                "            \"isDefault\": false,\n" +
                "            \"splits\": [\n" +
                "              {\n" +
                "                \"variationId\": 10,\n" +
                "                \"id\": 11,\n" +
                "                \"rolloutPercentage\": 100,\n" +
                "                \"delete\": false\n" +
                "              }\n" +
                "            ],\n" +
                "            \"id\": 10,\n" +
                "            \"conditions\": [\n" +
                "              {\n" +
                "                \"attributeId\": 9,\n" +
                "                \"op\": \"LTE\",\n" +
                "                \"id\": 3,\n" +
                "                \"attribute\": \"max_loan\",\n" +
                "                \"type\": \"boolean\",\n" +
                "                \"value\": \"500\",\n" +
                "                \"delete\": false\n" +
                "              },\n" +
                "              {\n" +
                "                \"attributeId\": 10,\n" +
                "                \"op\": \"GTE\",\n" +
                "                \"id\": 2,\n" +
                "                \"attribute\": \"min_loan\",\n" +
                "                \"type\": \"string\",\n" +
                "                \"value\": \"50\",\n" +
                "                \"delete\": false\n" +
                "              },\n" +
                "              {\n" +
                "                \"attributeId\": 8,\n" +
                "                \"op\": \"EQ\",\n" +
                "                \"id\": 1,\n" +
                "                \"attribute\": \"account_type\",\n" +
                "                \"type\": \"string\",\n" +
                "                \"value\": \"prepaid\",\n" +
                "                \"delete\": false\n" +
                "              }\n" +
                "            ],\n" +
                "            \"priority\": 1,\n" +
                "            \"delete\": false\n" +
                "          },\n" +
                "          {\n" +
                "            \"isDefault\": false,\n" +
                "            \"splits\": [\n" +
                "              {\n" +
                "                \"variationId\": 10,\n" +
                "                \"id\": 14,\n" +
                "                \"rolloutPercentage\": 50,\n" +
                "                \"delete\": false\n" +
                "              },\n" +
                "              {\n" +
                "                \"variationId\": 9,\n" +
                "                \"id\": 15,\n" +
                "                \"rolloutPercentage\": 50,\n" +
                "                \"delete\": false\n" +
                "              }\n" +
                "            ],\n" +
                "            \"id\": 12,\n" +
                "            \"conditions\": [\n" +
                "              {\n" +
                "                \"attributeId\": 9,\n" +
                "                \"op\": \"LT\",\n" +
                "                \"id\": 6,\n" +
                "                \"attribute\": \"max_loan\",\n" +
                "                \"type\": \"boolean\",\n" +
                "                \"value\": \"400\",\n" +
                "                \"delete\": false\n" +
                "              },\n" +
                "              {\n" +
                "                \"attributeId\": 8,\n" +
                "                \"op\": \"EQ\",\n" +
                "                \"id\": 5,\n" +
                "                \"attribute\": \"account_type\",\n" +
                "                \"type\": \"string\",\n" +
                "                \"value\": \"prepaid\",\n" +
                "                \"delete\": false\n" +
                "              }\n" +
                "            ],\n" +
                "            \"priority\": 3,\n" +
                "            \"delete\": false\n" +
                "          }\n" +
                "        ],\n" +
                "        \"type\": \"boolean\",\n" +
                "        \"createdAt\": 1585990567000,\n" +
                "        \"archived\": false,\n" +
                "        \"enviornments\": null,\n" +
                "        \"variations\": [\n" +
                "          {\n" +
                "            \"configs\": {\n" +
                "              \"header_color\": \"green\"\n" +
                "            },\n" +
                "            \"color\": \"#5AED8B\",\n" +
                "            \"name\": \"Show Continental Pass\",\n" +
                "            \"description\": \"\",\n" +
                "            \"id\": 10,\n" +
                "            \"key\": \"true\",\n" +
                "            \"order\": 0,\n" +
                "            \"allowList\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"configs\": {\n" +
                "              \"font_size\": \"12\"\n" +
                "            },\n" +
                "            \"color\": \"#ED5E5A\",\n" +
                "            \"name\": \"Demo Flag\",\n" +
                "            \"description\": \"\",\n" +
                "            \"id\": 9,\n" +
                "            \"key\": \"false\",\n" +
                "            \"order\": 1,\n" +
                "            \"allowList\": null\n" +
                "          }\n" +
                "        ],\n" +
                "        \"clientSideAccess\": false,\n" +
                "        \"name\": \"DemoFlag\",\n" +
                "        \"id\": 5,\n" +
                "        \"state\": \"ACTIVE\",\n" +
                "        \"offVariation\": 10,\n" +
                "        \"prerequisiteFlags\": {},\n" +
                "        \"key\": \"demoflag\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"status\": {\n" +
                "    \"code\": \"200\"\n" +
                "  }\n" +
                "}";
    }
}
