// Watcher entity glow fragment shader
// Self-illuminated: ignores scene lighting
// White (OBSERVING) -> Red (HUNT) per state uniform
#version 150

in vec2 texCoord;
in vec3 worldNormal;
in float smokeOffset;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float GlowColor; // 0.0 = white, 1.0 = red
uniform float GlowIntensity;

void main() {
    vec4 base = texture(DiffuseSampler, texCoord);
    if (base.a < 0.05) discard;

    vec3 whiteTint = vec3(1.0, 0.95, 0.95);
    vec3 redTint = vec3(1.0, 0.2, 0.1);
    vec3 tint = mix(whiteTint, redTint, GlowColor);

    vec3 col = base.rgb * tint;
    col += tint * GlowIntensity * 0.3;

    // Faint smoke wisps at edges
    float edge = pow(1.0 - base.a, 1.5);
    float wisp = sin(texCoord.x * 12.0 + Time * 1.5 + smokeOffset * 6.0) * 0.5 + 0.5;
    col += tint * edge * wisp * 0.15 * GlowIntensity;

    fragColor = vec4(col, base.a);
}
