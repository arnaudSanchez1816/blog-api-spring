export default function ThreeColumnLayout({ left, center, right }) {
    return (
        <div className="mx-auto pt-6 xl:grid xl:grid-cols-[1fr_65ch_1fr] xl:justify-center xl:gap-x-16">
            <div className="m-auto max-w-prose xl:m-0 xl:justify-self-end">
                {left}
            </div>
            <div className="m-auto mt-8 w-full max-w-prose xl:mt-0">
                {center}
            </div>
            <div className="mx-auto mt-24 max-w-prose xl:mx-0 xl:mt-0 xl:max-w-xs">
                {right}
            </div>
        </div>
    )
}
