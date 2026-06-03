// Mirror glass block fragment shader
// Red-tint, slight wave distortion, desaturated, NO real delayed framebuffer
// (delayed reflection requires framebuffer history pipeline; placeholder)
#version 150

in vec2 texCoord;
in vec3 worldPos;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float MirrorIntensity;
uniform float WaveAmount;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = texCoord;
    vec2 wave = vec2(
        sin(uv.y * 18.0 + Time * 1.5),
        cos(uv.x * 22.0 + Time * 1.1)
    ) * WaveAmount;

    vec3 src = texture(DiffuseSampler, uv + wave * 0.02).rgb;
    float luma = dot(src, vec3(0.299, 0.587, 0.114));
    vec3 gray = vec3(luma);
    vec3 col = mix(src, gray, 0.4);

    // Red tint
    col.r = mix(col.r, col.r * 1.2, MirrorIntensity);
    col.g = mix(col.g, col.g * 0.7, MirrorIntensity);
    col.b = mix(col.b, col.b * 0.6, MirrorIntensity);

    // Edge darkening
    float edge = smoothstep(0.0, 0.15, uv.x) * smoothstep(0.0, 0.15, uv.y)
               * smoothstep(0.0, 0.15, 1.0 - uv.x) * smoothstep(0.0, 0.15, 1.0 - uv.y);
    col *= 0.5 + 0.5 * edge;

    // Grain noise overlay
    float n = hash(uv * 800.0 + Time * 30.0) - 0.5;
    col += vec3(n) * 0.05 * MirrorIntensity;

    fragColor = vec4(col, 1.0);
}
