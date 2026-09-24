---
name: Literary Warmth
colors:
  surface: '#faf9f6'
  surface-dim: '#dbdad7'
  surface-bright: '#faf9f6'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f3f1'
  surface-container: '#efeeeb'
  surface-container-high: '#e9e8e5'
  surface-container-highest: '#e3e2e0'
  on-surface: '#1a1c1a'
  on-surface-variant: '#524534'
  inverse-surface: '#2f312f'
  inverse-on-surface: '#f2f1ee'
  outline: '#857462'
  outline-variant: '#d7c3ae'
  surface-tint: '#835500'
  primary: '#835500'
  on-primary: '#ffffff'
  primary-container: '#f5a623'
  on-primary-container: '#644000'
  inverse-primary: '#ffb955'
  secondary: '#6c5b51'
  on-secondary: '#ffffff'
  secondary-container: '#f6ded1'
  on-secondary-container: '#726157'
  tertiary: '#6d5a50'
  on-tertiary: '#ffffff'
  tertiary-container: '#c9b1a5'
  on-tertiary-container: '#55433a'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffddb4'
  primary-fixed-dim: '#ffb955'
  on-primary-fixed: '#291800'
  on-primary-fixed-variant: '#633f00'
  secondary-fixed: '#f6ded1'
  secondary-fixed-dim: '#d9c2b6'
  on-secondary-fixed: '#251911'
  on-secondary-fixed-variant: '#53443a'
  tertiary-fixed: '#f7ddd0'
  tertiary-fixed-dim: '#dac2b5'
  on-tertiary-fixed: '#261911'
  on-tertiary-fixed-variant: '#54433a'
  background: '#faf9f6'
  on-background: '#1a1c1a'
  surface-variant: '#e3e2e0'
typography:
  display-lg:
    fontFamily: Playfair Display
    fontSize: 56px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Playfair Display
    fontSize: 38px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.01em
  display-md:
    fontFamily: Playfair Display
    fontSize: 44px
    fontWeight: '600'
    lineHeight: 52px
    letterSpacing: -0.02em
  display-md-mobile:
    fontFamily: Playfair Display
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 38px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Playfair Display
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Playfair Display
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Playfair Display
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 30px
  headline-sm:
    fontFamily: Playfair Display
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 26px
  title-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 3rem
  margin-mobile: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system establishes a tactile, high-fidelity experience bridging contemporary digital commerce with the tactile romanticism of an independent antiquarian bookstore. It serves bibliophiles, casual readers, and collectors who value curation, provenance, and the sensory calm of print culture.

The visual direction pairs modern minimalism—generous negative space, structured typographic hierarchies, and fluid interaction flows—with physical metaphors drawn from bookbinding, deckled paper edges, and ambient reading room light. The interface rejects loud digital gimmickry in favor of serene editorial balance, inviting focused browsing, thoughtful curation, and quiet discovery.

## Colors

The palette establishes an organic, paper-forward tone. 

- **Primary (`#F5A623`)**: Used for high-intent conversion actions, key highlights, book condition indicators (e.g., "First Edition", "Signed"), and dynamic interactive states.
- **Secondary (`#4A3B32` - Warm Coffee Brown)**: Serves as the primary structural tone for UI frames, section dividers, and primary body copy, maintaining softer contrast than harsh pure blacks.
- **Tertiary (`#2C1E16` - Deep Sepia)**: Reserved for prominent editorial headlines, titles, and maximum-contrast textual accents.
- **Neutral Surface Hierarchy**: 
  - Canvas Base: Crisp White (`#FFFFFF`) for pure clarity in content-dense grids.
  - Page Surface: Warm Cream (`#FAF9F6`) for cards, flyout drawers, and search overlays.
  - Secondary Container: Parchment tint (`#F4EFE6`) for input fields, badges, and contextual callouts.
  - Borders & Rules: Sepia tint (`#E8DFD5` at 100% or `#4A3B32` at 12% opacity) to provide paper-like structural definition without visual heaviness.

## Typography

The typography couples the literary gravitas of `Playfair Display` with the functional neutrality and legibility of `Inter`. 

- **Display & Headlines**: Rendered in `Playfair Display` with tight letter spacing and proportional leading to evoke artisanal book title pages and editorial mastheads.
- **Body & Captions**: Handled by `Inter` across optical weights of 400 and 500 to deliver effortless reading performance in product specifications, edition descriptions, and customer reviews.
- **Metadata & System Labels**: Small caps or uppercase tracked labels (`label-md`, `label-sm`) are rendered in medium/semi-bold `Inter` to mimic stamped library cards, cataloging codes, and ISBN demarcations.

## Layout & Spacing

The layout adheres to an adaptive 12-column grid system (desktop) collapsing gracefully into 8 columns (tablet) and 4 columns (mobile), constrained to a maximum content container width of 1440px.

- **Grid Dynamics**: Gutters remain generous (`1.5rem` desktop, `1rem` mobile) to emulate wide page margins found in high-end print publishing.
- **Vertical Cadence**: Spacing tokens follow an 8-point base rhythm, standardizing component clearances (`space-sm` to `space-md`) and sectional pauses (`space-lg` to `space-xl`).
- **Edge Restraint**: Page-level gutters ensure content never binds tightly against the viewport, ensuring that even on narrow devices, the visual experience feels open, tranquil, and curated.

## Elevation & Depth

Elevation is achieved through ambient, warm-tinted diffusion rather than harsh drop shadows, referencing soft, natural light cast across cream paper stocks.

- **Level 0 (Flat)**: Pure `#FAF9F6` or `#FFFFFF` planes bordered by subtle `#4A3B32` lines at 10% opacity.
- **Level 1 (Card Rest)**: `0 2px 8px -2px rgba(44, 30, 22, 0.05), 0 1px 4px -1px rgba(44, 30, 22, 0.03)`. Used for standard book tiles and interactive listing rows.
- **Level 2 (Hover & Focus)**: `0 12px 24px -6px rgba(44, 30, 22, 0.08), 0 4px 8px -2px rgba(44, 30, 22, 0.04)`. Accompanied by a gentle -2px vertical translation on desktop.
- **Level 3 (Overlays & Drawers)**: `0 20px 40px -8px rgba(44, 30, 22, 0.12), 0 8px 16px -4px rgba(44, 30, 22, 0.06)`. Used for quick-view modals, cart slide-outs, and faceted filter panels.
- **Backdrop**: Modals and slide-outs feature an amber-sepia blurred scrim (`rgba(44, 30, 22, 0.35)` with `backdrop-filter: blur(6px)`).

## Shapes

The interface embraces organic curvature to soften technical edges without feeling childish. 

- **Containers & Cards**: Styled with `rounded-xl` (1.5rem / 24px) to present book jackets and editorial collections inside soft, protective bounds.
- **Interactive Controls**: Buttons, inputs, and selection chips utilize `rounded-lg` (1rem / 16px) or fully rounded pill configurations for tags.
- **Media Containers**: Cover art and book displays maintain gentle 8px corner radii, evoking traditional cloth-bound and paperback boards.

## Components

- **Buttons**:
  - *Primary*: Filled with vibrant warm amber (`#F5A623`), text in deep sepia (`#2C1E16`, `label-lg`), pill or `rounded-lg` radius. Subtle inner golden bevel and warm ambient shadow on hover.
  - *Secondary*: Canvas cream background (`#FAF9F6`) with a 1.5px border in `#4A3B32` (20% opacity), text in `#4A3B32`.
  - *Ghost / Editorial*: Underlined serif links (`Playfair Display`) with warm amber underline decoration.
- **Book Cards**:
  - Built on a crisp `#FAF9F6` ground with a 1px border of `#4A3B32` (8% opacity) and `rounded-xl` geometry.
  - Book cover imagery sits inside a slight inset recessed frame with a 1-pixel spine shadow to emulate physical depth.
  - Condition badges ("Fine", "Very Good", "Rare Collector") anchor the top-left corner.
- **Chips & Condition Tags**:
  - Compact pills with background `#F4EFE6`, text in `#4A3B32` (`label-md`), and optional warm amber dot indicators for grade verification.
- **Inputs & Search**:
  - Broad, welcoming search fields featuring `rounded-xl` borders, tinted background `#FAF9F6`, and a focused state marked by a 1.5px ring of `#F5A623`.
  - Text prompts set in `body-md` with coffee-brown placeholder tint.
- **Lists & Faceted Filters**:
  - Minimal list rows separated by 1px rules tinted `#E8DFD5`. Checkbox inputs utilize custom squircle checks tinted `#F5A623` when selected.
- **Provenance Badges**:
  - Specialized stamp-styled micro-components highlighting historical annotations, previous owner inscriptions, and edition notes.