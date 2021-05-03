#DOTCMS_CORE


This release includes the following code fixes:

1. https://github.com/dotCMS/core/issues/18364 : All Vanity URLs intermittently stop working, cache flush required to fix

2. https://github.com/dotCMS/core/issues/18214 : Spaces in File Assets are replaced with "+" instead of "%20"

3. https://github.com/dotCMS/core/issues/18479 : [core] : Key Value field is not returning data in correct order

4. https://github.com/dotCMS/core/issues/18319 : [OSGi] : Concurrent access to the bundle cache is causing problems

5. https://github.com/dotCMS/core/issues/18369 : Cannot edit file asset with indexed Binary field containing specific file content
   NOTE: Changes related to the new drag-and-drop were not included as it doesn't exist in 5.2.8 yet.

6. https://github.com/dotCMS/core/issues/18525 : [core] : Get related parents fails when related content is archived

7. https://github.com/dotCMS/core/issues/18501 : Race condition when Felix inits

8. https://github.com/dotCMS/core/issues/18626 : [WebDAV] : Default Index Policy for files is failing intermittently

9. https://github.com/dotCMS/core/issues/18621 : [Push Publishing] : Wrapper object is dropping the Identifier value

10. https://github.com/dotCMS/core/issues/18616 : Converting relationships results in a new relationship inode

11. https://github.com/dotCMS/core/issues/18245 : MonitorResource consumes too many resources

12. https://github.com/dotCMS/core/issues/18072 : [core] : Logging improvement when content reindex fails

13. https://github.com/dotCMS/core/issues/18673 : [Reindex] : Inconsistent data is causing the reindex to fail

14. https://github.com/dotCMS/core/issues/18641 : [Content Export] : Code improvement required to remove content export limit.

15. https://github.com/dotCMS/core/issues/18697 : [Workflows] : The AVAILABLE WORKFLOW ACTIONS button is failing with specific Lucene queries

16. https://github.com/dotCMS/core/issues/18764 : dojo/parser error in console when adding more than 2 relationship fields - 5.2x

17. https://github.com/dotCMS/core/issues/18848 : [rest] : Bad request when passing 'uri' parameter to endpoint '/v1/folder/sitename/{siteName}/uri/{uri : .+}'

18. https://github.com/dotCMS/support/tree/master/hotfixes/hotfix-legacy-ids_v5.0.3 : Fixes issues related to IDs that are not valid UUIDs
    NOTE: As per Will's request, the fix included in this plugin is intended to be a patch, and will not make it to the official distribution.

19. https://github.com/dotCMS/core/issues/18855 : NPE in ContainerWebAPI.getPersonalizedContentList

20. https://github.com/dotCMS/core/issues/18292 : When try to submit a form get permission error

21. https://github.com/dotCMS/core/issues/18744 : Global URLMaps

22. https://github.com/dotCMS/core/issues/18187 : Aliases not working correctly

23. https://github.com/dotCMS/core/issues/18964 : [REST] : Missing endpoint for retrieving folder tree 

24. https://github.com/dotCMS/core/issues/18920 : [Push Publishing] : Existing archived content is causing push to fail

25. https://github.com/dotCMS/core/issues/18951 : [Reindex] : Process is intermittently failing to retrieve content parent folder

26. https://github.com/dotCMS/core/issues/19854 : [core] : Page Mode set incorrectly with limited user

27. https://github.com/dotCMS/core/issues/19831 : [core] : URL validation for new pages under Site root is not correct

28. https://github.com/dotCMS/core/issues/19796 : depth>=3 doesn't work for self-related content

29. https://github.com/dotCMS/core/issues/19753 : Thumbnail creator filter is not working for .pdf files

30. https://github.com/dotCMS/core/issues/19715 : Make XStream initialization static

31. https://github.com/dotCMS/core/issues/19728	: graphql query of [Blog] -> [related File with Category field] produces error

32. https://github.com/dotCMS/core/issues/19686 : Error when editing multilingual content that is referenced on monolingual HTML pages

33. https://github.com/dotCMS/core/issues/19621	: Stop eating errors

34. https://github.com/dotCMS/core/issues/19608 : Contentlets lose inherited permissions until cache is flushed

35. https://github.com/dotCMS/core/issues/19566 : [logging] : General logging improvements

36. https://github.com/dotCMS/core/issues/19513	: Fix possible NPE in MimeTypeUtils

37. https://github.com/dotCMS/core/issues/19486	: Wrong defaults

38. https://github.com/dotCMS/core/issues/19452	: URL maps which match the URL map pattern and content values fail with 404 on customer system

39. https://github.com/dotCMS/core/issues/19337	: Content Search screen not filtering on "select" fields - 5.2x+ - reproducable in demo

40. https://github.com/dotCMS/core/issues/18927	: Remove unneeded icu4j

41. https://github.com/dotCMS/core/issues/18605	: ReindexThread always runs - never stops

42. https://github.com/dotCMS/core/issues/18505	: JSONTool does not return sub arrays

43. https://github.com/dotCMS/core/issues/18051	: PP a vtl file with no content throws an NPE

44. https://github.com/dotCMS/core/issues/19940 : [core] : Adjusting default ES settings for concurrent requests

45. https://github.com/dotCMS/core/issues/19890	: Custom Page Layout is not sending in Push PublishCustom Page Layout is not sending in Push Publish

46. https://github.com/dotCMS/core/issues/19910	: Google Translate Sub-action is sending error with even with valid translation key

47. https://github.com/dotCMS/core/issues/19832	: [core] : Legacy IDs are not compatible with Shorty API

48. https://github.com/dotCMS/core/issues/19927	: Adding more log to the JsonTOOL

49. https://github.com/dotCMS/core/issues/19319	: Allow shutdown from backend of dotCMS

50. https://github.com/dotCMS/core/issues/20053 : Anonymous users cannot fire actions when specified by their identifier

51. https://github.com/dotCMS/core/issues/20156 : User is logged out when accessing content if the Role does not have the 'Content' portlet tool group

52. https://github.com/dotCMS/core/issues/20068 : Allow portal.properties to be overridden by environmental variables

53. https://github.com/dotCMS/core/issues/20063 : Send Cookies Secure and HttpOnly

54. https://github.com/dotCMS/core/issues/20013 : Potential timezone bug

55. https://github.com/dotCMS/core/issues/20136 : [Push Publish] : Selecting the REMOVE option in Push Publishing modal is not working

56. https://github.com/dotCMS/core/issues/19993 : [workflow] : The "Send an Email" sub-action fails if executed before "Save content" sub-action 

57. https://github.com/dotCMS/core/issues/19951 : Date time field, should respect the time zone from format or company

58. https://github.com/dotCMS/core/issues/19926 : NPE on every page request after setting: ENABLE_NAV_PERMISSION_CHECK=true

59. https://github.com/dotCMS/core/issues/19877 : [core] : Remove unnecessary Web Token

60. https://github.com/dotCMS/core/issues/20041 : Time machine is causing an error with the index

61. https://github.com/dotCMS/core/issues/19813 : Allow mail session to be configured via environmental variables

62. https://github.com/dotCMS/core/issues/20197 : Cannot relate content to a macrolanguage if there is the same language with a country code

63. https://github.com/dotCMS/core/issues/20164 : JsonTool parsing

64. https://github.com/dotCMS/core/issues/20250 : [Push Publishing] : Improving error message when finding unique content match

65. https://github.com/dotCMS/core/issues/20232 : [Integrity Checker] : Improving error message when fixing File Asset conflict