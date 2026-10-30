---
version: alpha
name: JadwalStudio
description: Design system for JadwalStudio Android planner application, derived from Figma evidence and Jetpack Compose implementation.
colors:
  primary: "#4F7366"
  primary-dark: "#2E4841"
  background: "#F5F3ED"
  surface: "#FFFFFF"
  surface-card-1: "#DCE0D5"
  surface-card-2: "#F1ECE5"
  border-card: "#E5E0D3"
  text-primary: "#1A201C"
  text-muted: "#7A8680"
  accent-urgent: "#D9534F"
  accent-lightning: "#C4654D"
  nav-bg: "#F0ECE5"
  nav-border: "#DFD9CE"
typography:
  headline-display:
    fontFamily: Lora
    fontSize: 24px
    fontWeight: 700
    lineHeight: 1.2
  headline-title:
    fontFamily: Lora
    fontSize: 20px
    fontWeight: 700
    lineHeight: 1.3
  body-primary:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: 500
    lineHeight: 1.4
  body-secondary:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: 400
    lineHeight: 1.4
  label-badge:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: 700
    lineHeight: 1.2
rounded:
  sm: 8px
  md: 14px
  card: 20px
  header: 28px
  full: 9999px
---

## Overview

JadwalStudio is a personal school and task planner application built with an earthy, focused, and calm visual identity. It balances classic academic structure (serif typography, natural sage green `#4F7366`, and warm cream `#F5F3ED`) with clean, readable mobile UI components.

## Colors

The palette is strictly grounded in natural earth and foliage tones:

- **Primary (`#4F7366`)**: Governing brand color used for the distinctive curved top app header, primary action buttons, and active indicators.
- **Primary Dark (`#2E4841`)**: Deep forest green utilized for high-contrast titles, card headers, and prominent labels.
- **Background (`#F5F3ED`)**: Warm paper cream canvas providing comfort during extended reading and planning.
- **Surface (`#FFFFFF`)**: Pure white reserved for actionable cards and modal dialogs.
- **Surface Card 1 (`#DCE0D5`)**: Soft muted sage card container for current project summaries and progress modules.
- **Surface Card 2 (`#F1ECE5`)**: Warm sand container for secondary schedule items and neutral information tiles.
- **Border Card (`#E5E0D3`)**: Subtle warm sand border for flat cards without relying on heavy shadows.
- **Accent Urgent (`#D9534F`)**: Solid terracotta red used strictly for urgent deadlines and overdue warnings.
- **Nav Background (`#F0ECE5`)** & **Nav Border (`#DFD9CE`)**: Neutral warm base for the persistent bottom navigation bar.

## Typography

Typographic hierarchy relies on pairing a refined editorial serif with a crisp, geometric sans-serif:

- **Serif (`Lora`)**: Used exclusively for brand identity, screen headlines, section titles, and modal headers.
- **Sans-serif (`Plus Jakarta Sans`)**: Used for all functional UI elements, body text, form inputs, metadata chips, and timestamps.

## Layout

- **Mobile Viewport Target**: 390 × 844 dp (Android standard density scale).
- **Curved Top Header**: Signature brand surface fixed to the top with a `28px` bottom-edge radius.
- **Persistent Bottom Bar**: Fixed 64dp bottom navigation containing 3 core tabs (Dashboard, Tugas, Jadwal).
- **Horizontal Screen Padding**: 16dp to 20dp margin across all content lists.

## Elevation & Depth

Visual hierarchy is achieved through **Tonal Layering** and **Subtle Structural Borders**, never heavy drop shadows or arbitrary blur filters:

- Cards rest directly on the `#F5F3ED` canvas, defined by solid surface fills (`#DCE0D5`, `#F1ECE5`, `#FFFFFF`) and a `1px` border (`#E5E0D3`).
- Floating elements (dialogs) use a subtle elevation of `2dp` to `4dp` maximum.

## Shapes

- **Top Header**: `28px` bottom radius (`rounded-b-[28px]`).
- **Feature Cards**: `20px` corner radius.
- **Dialogs & Sub-cards**: `14px` corner radius.
- **Badges & Action Pills**: `9999px` full capsule radius.

## Do's and Don'ts

### Do's
- Do preserve the locked authentication, onboarding, and password-reset flows exactly as designed.
- Do keep backgrounds flat, warm, and matte (`#F5F3ED`).
- Do use solid color fills for cards (`#DCE0D5`, `#F1ECE5`, `#FFFFFF`).
- Do limit urgent accent colors (`#D9534F`) strictly to tasks with immediate deadlines.
- Do ensure all text contrast meets WCAG AA standards against cream and sage backgrounds.

### Don'ts
- Don't use gradients, purple tints, or multicolor backgrounds.
- Don't use glassmorphism, blur filters, or glowing edge effects.
- Don't add decorative AI clutter, animated pulsing dots, or unrequested emojis in the UI.
- Don't alter the established color tokens or introduce competing primitive libraries.
