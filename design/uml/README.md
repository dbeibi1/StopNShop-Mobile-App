# StopNShop UML exports

Prepared 6 October 2026 from the implemented version 1.3 Java/XML source. These are four logical diagrams; the class diagram also has two panels for readable portrait pages.

| Diagram | Editable vector | Report figure | Semantic companion |
|---|---|---|---|
| Use case | [SVG](01-use-case-diagram.svg) | [PNG](01-use-case-diagram.png) | [Mermaid](01-use-case-diagram.mmd) |
| Class, combined | [SVG](02-class-diagram.svg) | [PNG](02-class-diagram.png) | [Mermaid](02-class-diagram.mmd) |
| Activity | [SVG](03-activity-diagram.svg) | [PNG](03-activity-diagram.png) | [Mermaid](03-activity-diagram.mmd) |
| Application navigation | [SVG](04-navigation-diagram.svg) | [PNG](04-navigation-diagram.png) | [Mermaid](04-navigation-diagram.mmd) |

For portrait Word pages, use [class domain panel SVG](02a-class-domain.svg)/[PNG](02a-class-domain.png) and [class integration panel SVG](02b-class-integration.svg)/[PNG](02b-class-integration.png). The combined class figure is suitable for electronic zoom viewing.

The full local workspace also contains a five-page Word UML module and the complete report draft. Those Word documents are preserved locally and are outside this initial app-source upload. Edit the external SVG to change diagram shapes.

## Source and regeneration

The conventional UML SVGs are the vector figure sources. JSON files record generated geometry. Mermaid companions describe the same main semantics with schematic layout; they were not rendered with a Mermaid engine and do not reproduce the conventional actor/oval figure layout.

Local regeneration helpers and verification records remain in the complete development workspace. For this repository, SVGs can be opened in a vector editor and exported to PNG; JSON snapshots are reference outputs rather than input editors. Preserve readable labels and review all figures after editing.

## Reading the diagrams

Class members are selected for readability; `(...)` abbreviates parameters. `+` is public, `-` private and `#` protected. Italic handling information is abstract; underlined members are static. Filled diamonds show owned parts; dashed arrows show dependencies; solid arrows show references. Repeated domain-reference boxes in the integration panel refer to the same classes in the domain panel. Nested UI/helper types and most getters are omitted.

MainActivity hosts seven XML destinations. Home/Categories/Cart tabs operate on the five shopping destinations; Checkout and Confirmation hide them. Product Details Back restores Home or Product List and its previous scroll position. Product List Back uses its saved Home/Categories/Cart origin. The navigation figure uses notes for these global routes to keep its primary arrows readable.

In the activity diagram, “Review valid?” means the token is active and the current cart matches the frozen review. A completed token returns its original order. The one-second splash and session belong to the process; a new process starts fresh. All checkout behavior is simulated, with no real payment or dispatch.

The local milestone 27 and its verification records document the original source review and Word figure QA. Android source and the accepted APK are included in this repository; original phone screenshots and supporting records are preserved in the full local workspace.
